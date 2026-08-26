<?php

namespace App\Http\Controllers;
use App\Models\AuthenticatedUser;
use App\Models\Bid;
use App\Models\Auction;
use Illuminate\Support\Facades\Auth;
use Illuminate\Http\Request;

class BidController extends Controller
{
    public function myBids($id, $pageNr)
    {
        // Retrieve the authenticated user
        $user = Auth::user();
    
        if (!$user) {
            return abort(403);
        }
    
        // Define the number of bids to display per page
        $perPage = 5;
    
        // Calculate the offset based on the page number
        $offset = ($pageNr - 1) * $perPage;
    
        // Retrieve bids with associated auctions and paginate them
        $bids = Bid::with('auctions')
            ->where('user', $user->id) // Replace 'user_id_column_name' with the actual column name
            ->orderBy('creation_date', 'desc') // Order by most recent
            ->paginate($perPage, ['*'], 'page', $pageNr); // Paginate with the specified per page limit and page number
    
        // Pass the $id variable and the current page number to the view
        return view('pages.my_bids', compact('bids', 'id', 'pageNr'));
    }    

    /**
     * Display a listing of the resource.
     */
    public function index()
    {
        //
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
        //
    }

    /**
     * Display the specified resource.
     */
    public function show(Bid $bid)
    {
        //
    }

    /**
     * Show the form for editing the specified resource.
     */
    public function edit(Bid $bid)
    {
        //
    }

    /**
     * Update the specified resource in storage.
     */
    /*public function update(Request $request, Bid $bid)
{   
    // Get the bid amount from the form submission
    $bidAmount = $request->input('bid_amount');
    echo $request;
    $username = auth()->user()->username; // Fetch username from authenticated user
    $rating = auth()->user()->rating;
    
    // Perform actions with bid, then return the data as JSON
    $data = [
        'place' => 'Some place', // Update with actual bid place
        'rating' => $rating, // Update with actual bid rating
        'username' => $username,
        'bid_amount' => $bidAmount,
    ];
    
    return response()->json($data);
}*/

    public function placeBid(Request $request, $id)
    {
    try {
        // Validate the request, e.g., check if the bid amount is valid

        // Fetch the auction using the $id
        $auction = Auction::find($id);

        // Check if the auction exists and is open for bidding

        // Create and save the new bid
        $bid = new Bid;
        $bid->value = $request->bid_amount;
        $bid->auction = $id;
        $bid->user = auth()->user()->id; // Assuming you have user authentication
        $bid->save();

        // Update the auction's current price
        $auction->current_price = $request->bid_amount;
        $auction->save();

        // Redirect back to the auction details page with a success message
        return redirect()->back()->with('success', 'Bid placed successfully!');
        } catch (\Illuminate\Database\QueryException $ex) {
            // Capture the SQL error message
            $errorMessage = $ex->getMessage();
    
            // Regular expression to find text between "ERROR:" and "CONTEXT"
            $pattern = '/ERROR:\s*(.*?)\s*CONTEXT:/';
            if (preg_match($pattern, $errorMessage, $matches)) {
                // The custom message is in $matches[1]
                $customMessage = trim($matches[1]);
            } else {
                $customMessage = 'An unexpected error occurred. Please try again.';
            }
    
            return redirect()->back()->with('error', $customMessage);
        }
    }

    /**
     * Remove the specified resource from storage.
     */
    public function destroy(Bid $bid)
    {
        //
    }
}
