<?php

namespace App\Http\Controllers;

use App\Models\Auction;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use App\Models\AuthenticatedUser;
use App\Models\Transaction;
use App\Models\Item;
use App\Policies\AuctionPolicy;
use App\Models\ReportAuction;
use Carbon\Carbon;
use Illuminate\Support\Facades\DB;

class AuctionController extends Controller
{
    /**
     * Display a listing of the resource.
     */
    public function index(Request $request, $pageNr = 1)
    {
        $perPage = 5; // Number of auctions per page
        $query = $request->input('query'); // Get the search query from the request
        $minPrice = $request->input('min-price'); // Get the minimum price from the request
        $maxPrice = $request->input('max-price'); // Get the maximum price from the request
        $condition = $request->input('condition'); // Get the condition filter from the request
        $category = $request->input('category'); // Get the selected category filter from the request
        $sort = $request->input('sort', 'closest');
    
        // Initialize a query builder for auctions
        $auctionsQuery = Auction::query();
    
        // If a search query is provided, filter auctions based on the search criteria
        if ($query) {
            $auctionsQuery->whereRaw("tsvectors @@ to_tsquery('english', ?)", [$query]);
        }
    
        // Filter auctions with 'status' as "ACTIVE"
        $auctionsQuery->where('status', 'ACTIVE');
    
        // Filter auctions by price range if min and max prices are provided
        if ($minPrice !== null) {
            $auctionsQuery->where('current_price', '>=', $minPrice);
        }
        if ($maxPrice !== null) {
            $auctionsQuery->where('current_price', '<=', $maxPrice);
        }
    
        // Filter auctions by condition if a condition is selected
        if ($condition) {
            $auctionsQuery->whereHas('items', function ($query) use ($condition) {
                $query->where('condition', $condition);
            });
        }

        if ($category) {
            $auctionsQuery->whereHas('items', function ($query) use ($category) {
                $query->where('category', 'ILIKE', '%' . $category . '%');
            });
        }
        switch ($sort) {
            case 'price_asc':
                $auctionsQuery->orderBy('current_price', 'asc');
                break;
            case 'price_desc':
                $auctionsQuery->orderBy('current_price', 'desc');
                break;
            case 'alpha_asc':
                $auctionsQuery->orderBy('title', 'asc'); 
                break;
            case 'alpha_desc':
                $auctionsQuery->orderBy('title', 'desc');
                break;
            case 'closest':
            default:
                $auctionsQuery->orderBy('end_date', 'asc'); 
                break;
        }
    
        // Paginate the results
        $auctions = $auctionsQuery->with('items')->paginate($perPage, ['*'], 'page', $pageNr);
    
        // Set the path for the paginator
        $auctions->appends([
            'query' => $query,
            'min-price' => $minPrice,
            'max-price' => $maxPrice,
            'condition' => $condition,
            'category' => $category,
            'sort' => $sort
        ])->links();
    
        return view('pages.auctions', compact('auctions', 'query', 'minPrice', 'maxPrice', 'condition', 'category', 'sort'));
    }

    public function showOwnedAuctions($id, $pageNr)
    {
        // Retrieve the authenticated user
        $user = Auth::user();
    
        if (!$user) {
            return abort(403);
        }
    
        // Retrieve the user by the provided $id
        $targetUser = AuthenticatedUser::find($id);
    
        if (!$targetUser) {
            return abort(404);
        }
    
        // Define the number of auctions to display per page
        $perPage = 5;
    
        // Calculate the offset based on the page number
        $offset = ($pageNr - 1) * $perPage;
    
        // Retrieve the auctions where the authenticated user is the owner and paginate them
        $ownedAuctions = $targetUser->auctions()
            ->orderBy('end_date', 'asc') 
            ->paginate($perPage, ['*'], 'page', $pageNr); // Paginate with the specified per page limit and page number
    
        return view('pages.owned_auctions', compact('ownedAuctions', 'id', 'pageNr'));
    }    

    /**
     * Show the form for creating a new resource.
     */
    public function create()
    {
        return view('pages.auctionCreate'); 
    }    

    
    /**
     * Store a newly created resource in storage.
     */
    public function store(Request $request)
    {
        try {
            if (!Auth::check()) {
                // Handle unauthenticated user scenario
                return redirect()->route('login'); // Redirect to login or another appropriate response
            }
    
            $user = Auth::user();
    
            // Combine validation rules for both Auction and Item
            $validatedData = $request->validate([
                'title' => 'required|string|max:255',
                'description' => 'required|string',
                'end_date' => 'required|date|after:start_date',
                'starting_price' => 'required|numeric|min:0',
                'name' => 'required|string|max:255',
                'category' => 'required|string|max:255',
                'brand' => 'required|string|max:255',
                'color' => 'required|string|max:255',
                'condition' => 'required|string|max:255',
                'picture' => 'required|image|mimes:jpeg,png,jpg,gif,svg|max:2048',
            ]);
    
            if ($request->hasFile('picture')) {
                $file = $request->file('picture');
                $originalExtension = $file->getClientOriginalExtension();
                $uniqueName = Carbon::now()->format('YmdHis') . '.' . $originalExtension;
                $filePath = $file->storeAs('pictures', $uniqueName, 'public');
                $validatedData['picture'] = $filePath;
            }

            $item = new Item($validatedData);
            $item->save();
            
            $auction = new Auction($validatedData);
            $auction->current_price = $validatedData['starting_price'];
            $auction->owner = $user->id;
            $auction->items()->associate($item); // Associate the auction with the item
            $auction->save();            
    
            return redirect()->route('auction.show', ['id' => $auction->id]);
        } catch (\Illuminate\Database\QueryException $ex) {
            // Capture the SQL error message
            $errorMessage = $ex->getMessage();
    
            // Regular expression to find text between "ERROR:" and "CONTEXT"
            $pattern = '/ERROR:(.*?)CONTEXT/';
            if (preg_match($pattern, $errorMessage, $matches)) {
                // The custom message is in $matches[1]
                $customMessage = trim($matches[1]);
            } else {
                $customMessage = 'An unexpected error occurred. Please try again.';
            }
    
            return redirect()->back()->with('error', $customMessage);
        }
    }    



    public function edit($id)
    {
        if (!Auth::check()) {
            // Handle unauthenticated user scenario
            return redirect()->route('login'); // Redirect to login or another appropriate response
        }
    
        $auction = Auction::findOrFail($id);
        $item = $auction->item; // Assuming a one-to-one relationship between Auction and Item
    
        // Return the edit view with the auction and item data
        return view('pages.auctionEdit', compact('auction', 'item'));
    }

    public function update(Request $request, $id)
    {
        if (!Auth::check()) {
            // Handle unauthenticated user scenario
            return redirect()->route('login');
        }
    
        $user = Auth::user();
    
        $auction = Auction::findOrFail($id);
    
        // Only update fields that can be changed
        $validatedAuctionData = $request->validate([
            'title' => 'required|string|max:255',
            'description' => 'required|string',
        ]);
    
        // Update the Auction
        $auction->fill($validatedAuctionData);
        $auction->save();
    
        return redirect()->route('auction.show', ['id' => $auction->id]);
    }

    /**
     * Display the specified resource.
     */
    public function show($id)
    {
        $auction = Auction::findOrFail($id);
    
        // Use the authorize method to check if the user is authorized to view the auction
        $this->authorize('viewAuction', $auction);
    
        // Order the bids by value in descending order for this auction
        $auction->load(['bids' => function ($query) {
            $query->orderBy('value', 'desc');
        }]);
    
        // Load the associated item (if necessary)
        $item = Item::findOrFail($auction->item);
        
        //$reports = ReportAuction::where('auction', $auction->id)->get();
        $reports = $auction->reports;


        return view('pages.showauction', compact('auction', 'item'));
    }

    public function cancel($id)
    {
        try {
            $auction = Auction::findOrFail($id);
        
            // Check if the user has the permission to cancel the auction (e.g., only the owner can cancel it)
            if (Auth::user()->id != $auction->owner) {
                return redirect()->back()->with('error', 'You do not have permission to cancel this auction.');
            }
        
            // Update the status to "CANCELLED"
            $auction->status = 'CANCELLED';
            $auction->save();
        
            return redirect()->route('auction.show', $id)->with('success', 'The auction has been canceled.');

        } catch (\Illuminate\Database\QueryException $ex) {
            // Capture the SQL error message
            $errorMessage = $ex->getMessage();
    
            // Regular expression to find text between "ERROR:" and "CONTEXT"
            $pattern = '/ERROR:(.*?)CONTEXT/';
            if (preg_match($pattern, $errorMessage, $matches)) {
                // The custom message is in $matches[1]
                $customMessage = trim($matches[1]);
            } else {
                $customMessage = 'An unexpected error occurred. Please try again.';
            }
    
            return redirect()->back()->with('error', $customMessage);
        }
    }    

    public function showReports($id)
{
    $auction = Auction::find($id);

    if (!$auction) {
        return redirect()->back()->with('error', 'Auction not found.');
    }

    $reports = ReportAuction::where('auction_id', $id)->get();

    return view('auction.reports', compact('auction', 'reports'));
}

    /**
     * Remove the specified resource from storage.
     */
    public function destroy($id)
    {
        $auction = Auction::findOrFail($id);
    
        // Perform authorization check here to ensure only admin can delete auctions
        if (Auth::user()->role != 'ADMIN') {
            abort(403, 'Unauthorized action.');
        }
    
        // Begin a database transaction
        DB::beginTransaction();
    
        try {
            // Get the highest bidder (if not null)
            $highestBid = $auction->bids()->orderBy('value', 'desc')->first();

            if ($highestBid) {
                $highestBidder = $highestBid->authenticatedUser;
                $refundAmount = $highestBid->value;
            
                // Start a database transaction
                DB::beginTransaction();
            
                try {
                    // Update highest bidder's balance
                    $highestBidder->update(['balance' => $highestBidder->balance + $refundAmount]);
            
                    // Create a new transaction record for the refund
                    $refundTransaction = new Transaction([
                        'value' => $refundAmount, // The amount to be refunded
                        'transaction_date' => now(), // The current date and time
                        'description' => 'Refund for deleted auction',
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
    
            // Delete all bids for the auction
            $auction->bids()->delete();
    
            // Delete the auction itself
            $auction->delete();
    
            // Refund the highest bidder (if exists)
    
            // Commit the database transaction
            DB::commit();
    
            return redirect()->route('auction.index', 1)->with('success', 'Auction deleted successfully.');
        } catch (\Exception $e) {
            // Rollback the transaction in case of an exception
            DB::rollBack();
    
            return redirect()->back()->with('error', 'An error occurred while deleting the auction.');
        }
    }

    public function follow(Auction $auction)
    {
        auth()->user()->followingAuctions()->attach($auction->id);
    
        // Redirect back to the same page
        return back();
    }
    
    public function unfollow(Auction $auction)
    {
        auth()->user()->followingAuctions()->detach($auction->id);
    
        // Redirect back to the same page
        return back();
    }    

    public function following($pageNr)
    {
        // Get the authenticated user
        $user = auth()->user();
    
        // Retrieve the auctions that the user is following with pagination
        $perPage = 5; // Change the number to the desired items per page
        $followingAuctions = $user->followingAuctions()
            ->orderBy('end_date', 'asc') 
            ->paginate($perPage, ['*'], 'page', $pageNr);
    
        return view('pages.following_auctions', compact('followingAuctions', 'pageNr'));
    }    
}
