<?php

namespace App\Http\Controllers;

use App\Models\AuthenticatedUser;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Auth;
use App\Policies\AuthenticatedUserPolicy;
use App\Http\Controllers\Controller;
use App\Models\Auction;
use App\Models\Bid;
use Illuminate\Support\Facades\DB;
use Illuminate\Database\Eloquent\ModelNotFoundException;
use Illuminate\Support\Facades\Storage;
use Carbon\Carbon;
use App\Models\Transaction;

class AuthenticatedUserController extends Controller
{
    /**
     * Display a listing of the resource.
     */
    public function index()
    {
        // Fetch auctions with the highest current price and bid count of 0 or more
        $topAuctions = Auction::where('status', 'ACTIVE')
        ->whereHas('bids', function($query) {
            $query->havingRaw('COUNT(*) > 0');
        }, '>=', 0)
        ->orderBy('current_price', 'desc')
        ->take(5)
        ->get();
    
        // Fetch top bidders
        $topBidders = Bid::select('user', DB::raw('COUNT(*) as total_bids'), DB::raw('SUM(value) as total_bid_amount'))
        ->groupBy('user')
        ->orderBy('total_bid_amount', 'desc')
        ->take(5)
        ->whereHas('authenticatedUser', function ($query) {
            $query->where('is_blocked', false);
        })
        ->with('authenticatedUser')
        ->get();    
    
        return view('pages.home', compact('topAuctions', 'topBidders'));
    }
    
    /**
     * Show the form for creating a new resource.
     */
    public function create()
    {
        //
    }

    /**
     * Store a newly created resource in storage.
     */
    public function store(Request $request)
    {
        // Validate the request data
        $validatedData = $request->validate([
            'username' => 'required|string|max:255|unique:authenticated_user',
            'first_name' => 'required|string|max:255',
            'last_name' => 'required|string|max:255',
            'email' => 'required|email|max:255|unique:authenticated_user',
            'password' => 'required|string|min:8'
        ]);

        // Hash the password
        $validatedData['password'] = Hash::make($validatedData['password']);

        // Create a new user with hashed password
        $user = AuthenticatedUser::create($validatedData);

        // Redirect or return a response as needed
        return redirect()->route('login')->with('success', 'User created successfully');
    }

    public function promoteToAdmin($id)
    {
        // Retrieve the user based on the $id parameter
        $user = AuthenticatedUser::find($id);
    
        if (!$user) {
            // Handle the case where the user is not found (e.g., show an error message)
            return abort(404);
        }
    
        // Check if the authenticated user has permission to promote users to ADMIN
        if (Auth::user()->role === 'ADMIN' && Auth::user()->id != $user->id) {
            // Check if the user has any "ACTIVE" auctions
            $hasActiveAuctions = $user->auctions()->where('status', 'ACTIVE')->exists();
    
            // Check if the user has any active bids
            $hasActiveBids = $user->bids()->whereHas('auctions', function ($query) {
                $query->where('status', 'ACTIVE');
            })->exists();
    
            if (!$hasActiveAuctions && !$hasActiveBids) {
                // User doesn't have active auctions or active bids, so promote them to ADMIN
                $user->role = 'ADMIN';
                $user->save();
    
                return back()->with('success', 'User promoted to ADMIN successfully');
            } else {
                // Handle the case where the user has active auctions or active bids
                return back()->with('error', 'User has active auctions or active bids and cannot be promoted to ADMIN.');
            }
        } else {
            // Handle the case where the authenticated user doesn't have permission
            return abort(403); // Return a 403 Forbidden response
        }
    }
    
    

    /**
     * Display the specified resource.
     */
    public function show($id)
    {
        $user = AuthenticatedUser::find($id);
    
        if (!$user) {
            return abort(404);
        }
    
        $this->authorize('viewProfile', $user);
    
        return view('pages.profile', compact('user'));
    }

    public function searchResults(Request $request)
    {
        $perPage = 5;
        $query = $request->input('query');

        // Query users based on the search criteria if a query is provided
        $usersQuery = AuthenticatedUser::query();
        
        if ($query) {
            $usersQuery->where('username', 'LIKE', '%' . $query . '%')
            ->orWhere('first_name', 'LIKE', '%' . $query . '%')
            ->orWhere('last_name', 'LIKE', '%' . $query . '%')
            ->orWhere('email', 'LIKE', '%' . $query . '%');
        }

        // Query auctions based on the search criteria if a query is provided
        $auctionsQuery = Auction::query();

        if ($query) {
            $auctionsQuery->whereRaw("tsvectors @@ to_tsquery('english', ?)", [$query]);
        }

        // Add a condition to filter auctions with 'status' as "ACTIVE"
        $auctionsQuery->where('status', 'ACTIVE');

        // Paginate the results for both users and auctions
        $users = $usersQuery->paginate($perPage, ['*'], 'users_page');
        $auctions = $auctionsQuery->paginate($perPage, ['*'], 'auctions_page');

        // Return the search results view
        return view('pages.search_results', compact('users', 'auctions', 'query'));
    }
      

    public function all(Request $request, $pageNr)
    {
        $perPage = 5; // Number of users per page
        $query = $request->input('query'); // Get the search query from the request
    
        // Query users based on the search criteria if a query is provided
        $usersQuery = AuthenticatedUser::query();
        
        if ($query) {
            $usersQuery->where('username', '=', $query)
                       ->orWhere('first_name', '=', $query)
                       ->orWhere('last_name', '=', $query)
                       ->orWhere('email', '=', $query);
        }
        
        // Paginate the results
        $users = $usersQuery->paginate($perPage, ['*'], 'page', $pageNr);
    
        return view('pages.all_users', compact('users', 'query'));
    }
    

    /**
     * Show the form for editing the specified resource.
     */
    public function edit($id)
    {
        // Retrieve the user based on the $id parameter
        $user = AuthenticatedUser::find($id);

        if (!$user) {
            // Handle the case where the user is not found (e.g., show an error message)
            return abort(404);
        }

        // Return the edit profile form view with the user's data
        return view('pages.editprofile', compact('user'));
    }

    /**
     * Update the specified resource in storage.
     */
    public function update(Request $request, $id)
    {
        // Retrieve the user based on the $id parameter
        $user = AuthenticatedUser::find($id);

        if (!$user) {
            // Handle the case where the user is not found (e.g., show an error message)
            return abort(404);
        }

        // Validate and update the user's information
        $validatedData = $request->validate([
            'username' => 'required|string|max:255|unique:authenticated_user,username,'.$id,
            'first_name' => 'required|string|max:255',
            'last_name' => 'required|string|max:255',
            'email' => 'required|email|max:255|unique:authenticated_user,email,'.$id,
            'password' => 'nullable|string|min:8', // Allow the password to be nullable if not changed
            'picture' => 'nullable|image|mimes:jpeg,png,jpg,gif,svg|max:2048',
        ]);

        // Hash the password if provided
        if ($request->filled('password')) {
            $validatedData['password'] = Hash::make($validatedData['password']);
        } else {
            // If the password is not provided, remove it from the data array
            unset($validatedData['password']);
        }

        if ($request->hasFile('picture')) {
            $file = $request->file('picture');
            $originalExtension = $file->getClientOriginalExtension();
            $uniqueName = Carbon::now()->format('YmdHis') . '.' . $originalExtension;
            $filePath = $file->storeAs('pictures', $uniqueName, 'public');
        
            // Update the validatedData array with the new file path
            $validatedData['picture'] = $filePath;
        }        
        
        // Update the user's information
        $user->update($validatedData);

        // Redirect the user to their profile page or another relevant page
        return redirect()->route('show', ['id' => $id])->with('success', 'Profile updated successfully');
    }

    public function balance($id)
    {
        $user = AuthenticatedUser::find($id);
    
        if (!$user) {
            return abort(404);
        }
    
        return view('pages.balance', compact('user'));
    }
    
    public function addFunds(Request $request, $id)
    {
        $user = AuthenticatedUser::find($id);
    
        if (!$user) {
            return abort(404);
        }
    
        $request->validate([
            'amount' => 'required|numeric|min:0',
        ]);
    
        $amount = $request->input('amount');
    
        // Start a database transaction
        DB::beginTransaction();
    
        try {
            // Update user's balance
            $user->balance += $amount;
            $user->save();
    
            // Create a new transaction record
            $transaction = new Transaction([
                'value' => $amount, // Use the added amount as the value
                'transaction_date' => now(), // Use the current date and time
                'description' => 'Funds added',
            ]);
    
            // Associate the transaction with the user using the relationship method
            $transaction->authenticatedUser()->associate($user);
            $transaction->save();
    
            // Commit the transaction
            DB::commit();
    
            return redirect()->route('show', ['id' => $id])->with('success', 'Funds added successfully');
        } catch (\Exception $e) {
            // Rollback the transaction in case of an error
            DB::rollback();
    
            \Log::error($e->getMessage()); // Log the error for debugging
    
            return redirect()->route('show', ['id' => $id])->with('error', 'Failed to add funds. Please try again.');
        }
    }
    
/*
    public function blockUser($id)
    {
        $user = AuthenticatedUser::find($id);

        if (!$user) {
            // Handle the case where the user is not found (e.g., show an error message)
            return abort(404);
        }

        // Check if the authenticated user has permission to promote users to ADMIN
        if (Auth::user()->role === 'ADMIN' && Auth::user()->id != $user->id) {
            // Check if the user has any "ACTIVE" auctions
            $hasActiveAuctions = $user->auctions()->where('status', 'ACTIVE')->exists();
    
            // Check if the user has any active bids
            $hasActiveBids = $user->bids()->whereHas('auctions', function ($query) {
                $query->where('status', 'ACTIVE');
            })->exists();
    
            if (!$hasActiveAuctions && !$hasActiveBids) {
                // User doesn't have active auctions or active bids, so promote them to ADMIN
                $user->is_blocked = TRUE;
                $user->save();
    
                return back()->with('success', 'User blocked successfully');
            } else {
                // Handle the case where the user has active auctions or active bids
                return back()->with('error', 'User has active auctions or active bids and cannot be blocked.');
            }
        } else {
            // Handle the case where the authenticated user doesn't have permission
            return abort(403); // Return a 403 Forbidden response
        }
      
    }*/

    /*public function unblockUser($id)
    {
        $user = AuthenticatedUser::find($id);

        if (!$user) {
            // Handle the case where the user is not found (e.g., show an error message)
            return abort(404);
        }

        // Check if the authenticated user has permission to promote users to ADMIN
        if (Auth::user()->role === 'ADMIN' && Auth::user()->id != $user->id) {
            // Check if the user has any "ACTIVE" auctions
            $hasActiveAuctions = $user->auctions()->where('status', 'ACTIVE')->exists();
    
            // Check if the user has any active bids
            $hasActiveBids = $user->bids()->whereHas('auctions', function ($query) {
                $query->where('status', 'ACTIVE');
            })->exists();
    
            if (!$hasActiveAuctions && !$hasActiveBids) {
                // User doesn't have active auctions or active bids, so promote them to ADMIN
                $user->is_blocked = FALSE;
                $user->save();
    
                return back()->with('success', 'User blocked successfully');
            } else {
                // Handle the case where the user has active auctions or active bids
                return back()->with('error', 'User has active auctions or active bids and cannot be blocked.');
            }
        } else {
            // Handle the case where the authenticated user doesn't have permission
            return abort(403); // Return a 403 Forbidden response
        }
      
    }
*/
    /**
     * Remove the specified resource from storage.
     */
    public function destroy(AuthenticatedUser $authenticatedUser)
    {
        //
    }

    public function showAboutUs()
    {
        
      return view('pages.aboutUs');
      
    }

    public function showFAQ()
    {
        
      return view('pages.faq');
      
    }
    
    public function showFeatures()
    {
        return view('pages.features');
    }

    public function showContacts()
    {
        return view('pages.contacts');
    }

    public function blockUser($id)
    {
        // Find the user by ID
        try {
            $user = AuthenticatedUser::findOrFail($id);
        } catch (ModelNotFoundException $e) {
            return redirect()->back()->with('error', 'User not found.');
        }
    
        // Check if the logged-in user is an admin and the target user is not an admin
        if (Auth::user()->role === 'ADMIN' && Auth::user()->id != $user->id && $user->role !== 'ADMIN') {
            DB::beginTransaction();
    
            try {
                // Find all ACTIVE auctions owned by the blocked user
                $auctions = Auction::where('owner', $id)->where('status', 'ACTIVE')->get();
    
                foreach ($auctions as $auction) {
                    // Find the highest bidder for this auction
                    try {
                        $highestBid = Bid::where('auction', $auction->id)->orderByDesc('value')->first();
    
                        if ($highestBid) {
                            // Check if the highest bidder is blocked
                            try {
                                $highestBidder = AuthenticatedUser::find($highestBid->user);
    
                                if ($highestBidder) {
                                    // Start a database transaction
                                    DB::beginTransaction();
                                
                                    try {
                                        // Update highest bidder's balance
                                        $highestBidder->update(['balance' => $highestBidder->balance + $highestBid->value]);
                                
                                        // Create a new transaction record for the refund
                                        $refundTransaction = new Transaction([
                                            'value' => $highestBid->value, // The amount to be refunded
                                            'transaction_date' => now(), // The current date and time
                                            'description' => 'Refund because of blocked user',
                                            // Make sure to associate the transaction with the user correctly
                                        ]);
                                        $refundTransaction->authenticatedUser()->associate($highestBidder);
                                        $refundTransaction->save();
                                
                                        // Commit the transaction
                                        DB::commit();
                                    } catch (\Exception $e) {
                                        // Rollback the transaction in case of an error
                                        DB::rollback();
                                
                                        \Log::error($e->getMessage()); // Log the error for debugging
                                        // Handle the exception (e.g., return an error response)
                                    }
                                }                                
                            } catch (\Exception $e) {
                                // Handle exception for highest bidder not found
                                return redirect()->back()->with('error', 'Error updating highest bidder: ' . $e->getMessage());
                            }
                        }
                    } catch (\Exception $e) {
                        // Handle exception for highest bid query
                        return redirect()->back()->with('error', 'Error querying highest bid: ' . $e->getMessage());
                    }
    
                    // Delete all bids related to this auction
                    try {
                        Bid::where('auction', $auction->id)->delete();
                    } catch (\Exception $e) {
                        // Handle exception for bid deletion
                        return redirect()->back()->with('error', 'Error deleting bids: ' . $e->getMessage());
                    }
    
                    // Change the auction status to "CANCELLED"
                    try {
                        $auction->update(['status' => 'CANCELLED']);
                    } catch (\Exception $e) {
                        // Handle exception for auction status update
                        return redirect()->back()->with('error', 'Error updating auction status: ' . $e->getMessage());
                    }
                }
    
                // Find all bids made by the blocked user and assign the "user" field to null
                try {
                    Bid::where('user', $id)->update(['user' => null]);
                } catch (\Exception $e) {
                    // Handle exception for updating bids
                    return redirect()->back()->with('error', 'Error updating bids made by blocked user: ' . $e->getMessage());
                }
    
                // Update the is_blocked status to true
                try {
                    $user->update(['is_blocked' => true]);
                } catch (\Exception $e) {
                    // Handle exception for updating user status
                    return redirect()->back()->with('error', 'Error updating user status: ' . $e->getMessage());
                }
    
                DB::commit();
    
                return redirect()->back()->with('success', 'User has been blocked, their bids have been deleted, and their auctions have been cancelled. The highest bidder has been refunded.');
            } catch (\Exception $e) {
                DB::rollBack();
    
                return redirect()->back()->with('error', 'An error occurred while blocking the user: ' . $e->getMessage());
            }
        } else {
            return redirect()->back()->with('error', 'You do not have permission to block this user.');
        }
    }  


    public function unblockUser($id)
    {
        $user = AuthenticatedUser::findOrFail($id);
    
        // Check if the logged-in user is an admin and the target user is not an admin
        if (Auth::user()->role === 'ADMIN' && Auth::user()->id != $user->id && $user->role !== 'ADMIN') {
            $user->update(['is_blocked' => false]);
            return redirect()->back()->with('success', 'User has been unblocked.');
        } else {
            return redirect()->back()->with('error', 'You do not have permission to unblock this user.');
        }
    }
    
    public function deleteUser($id)
    {
        try {
            // Find the user by ID
            $user = AuthenticatedUser::findOrFail($id);
        } catch (ModelNotFoundException $e) {
            return redirect()->route('show.users', ['pageNr' => 1])->with('error', 'User not found.');
        }
    
        // Check if the logged-in user is an admin and the target user is not an admin
        if (Auth::user()->role === 'ADMIN' || (Auth::user()->id === $user->id && Auth::user()->role !== 'ADMIN'))
        {
            DB::beginTransaction();
    
            try {
                // Find all auctions owned by the deleted user
                $auctions = Auction::where('owner', $id)->get();
    
                foreach ($auctions as $auction) {
                    // Find the highest bidder for this auction
                    try {
                        $highestBid = Bid::where('auction', $auction->id)->orderByDesc('value')->first();
    
                        if ($highestBid) {
                            // Find the highest bidder user
                            try {
                                $highestBidder = AuthenticatedUser::find($highestBid->user);
    
                                if ($highestBidder) {
                                    // Start a database transaction
                                    DB::beginTransaction();
                                
                                    try {
                                        // Update highest bidder's balance
                                        $highestBidder->update(['balance' => $highestBidder->balance + $highestBid->value]);
                                
                                        // Create a new transaction record for the refund
                                        $refundTransaction = new Transaction([
                                            'value' => $highestBid->value, // The amount to be refunded
                                            'transaction_date' => now(), // The current date and time
                                            'description' => 'Refund because of deleted user',
                                            // Make sure to associate the transaction with the user correctly
                                        ]);
                                        $refundTransaction->authenticatedUser()->associate($highestBidder);
                                        $refundTransaction->save();
                                
                                        // Commit the transaction
                                        DB::commit();
                                    } catch (\Exception $e) {
                                        // Rollback the transaction in case of an error
                                        DB::rollback();
                                
                                        \Log::error($e->getMessage()); // Log the error for debugging
                                        // Handle the exception (e.g., return an error response)
                                    }
                                }                                
                            } catch (\Exception $e) {
                                // Handle exception for highest bidder not found
                                throw new \Exception('Error updating highest bidder: ' . $e->getMessage());
                            }
                        }
                    } catch (\Exception $e) {
                        // Handle exception for highest bid query
                        throw new \Exception('Error querying highest bid: ' . $e->getMessage());
                    }
    
                    // Delete all bids related to this auction
                    try {
                        Bid::where('auction', $auction->id)->delete();
                    } catch (\Exception $e) {
                        // Handle exception for bid deletion
                        throw new \Exception('Error deleting bids: ' . $e->getMessage());
                    }
    
                    // Change the auction status to "CANCELLED"
                    try {
                        $auction->update(['status' => 'CANCELLED']);
                    } catch (\Exception $e) {
                        // Handle exception for auction status update
                        throw new \Exception('Error updating auction status: ' . $e->getMessage());
                    }
                }
    
                // Find all bids made by the deleted user and assign the "user" field to null
                try {
                    Bid::where('user', $id)->update(['user' => null]);
                } catch (\Exception $e) {
                    // Handle exception for updating bids
                    throw new \Exception('Error updating bids made by deleted user: ' . $e->getMessage());
                }
    
                // Delete the user after processing refunds, auction cancellations, and bid deletions
                $user->delete();
    
                DB::commit();
    
                return redirect()->route('show.users', ['pageNr' => 1])->with('success', 'User deleted successfully, their bids have been deleted, and their auctions have been cancelled. The highest bidder has been refunded.');
            } catch (\Exception $e) {
                DB::rollBack();
    
                return redirect()->route('show', ['id' => $id])->with('error', 'An error occurred while processing the deletion, refunds, bid deletions, and status update: ' . $e->getMessage());
            }
        } else {
            return redirect()->route('show', ['id' => $id])->with('error', 'You do not have permission to delete this user.');
        }
    }
    
}
