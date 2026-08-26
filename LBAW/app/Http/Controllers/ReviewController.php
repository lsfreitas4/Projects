<?php

namespace App\Http\Controllers;

use App\Models\Review;
use App\Models\Auction;
use Illuminate\Http\Request;

class ReviewController extends Controller
{
    /**
     * Display a listing of the resource.
     */
    public function index()
    {
        $userId = auth()->user()->id;
    
        $closedAuctions = Auction::where('status', 'CLOSED')
        ->whereHas('bids', function ($query) use ($userId) {
            $query->where('user', $userId)
                ->orderBy('value', 'desc')
                ->take(1);
        })
        ->whereDoesntHave('review') 
        ->get();
    
        return view('pages.reviews_user', compact('closedAuctions'));
    }
    

    /**
     * Show the form for creating a new resource.
     */
    public function create($auctionId)
    {
        $auction = Auction::find($auctionId);
    
        if (!$auction) {
            // Handle the case where the auction doesn't exist
            abort(404);
        }
    
        return view('pages.review_form', compact('auction'));
    }    

    /**
     * Store a newly created resource in storage.
     */
    public function store(Request $request)
    {
        // Validate the request data here
    
        $review = new Review();
        $review->rating = $request->input('rating');
        $review->title = $request->input('title');
        $review->description = $request->input('description');
        $review->date = now(); // You can adjust the date as needed
        $review->reviewer = auth()->user()->id;
        $review->auction = $request->input('auction_id');
        $auction = Auction::find($review->auction);

        if (!$auction) {
            abort(404);
        }
    
        $review->reviewed = $auction->authenticatedUser->id;
        $review->save();
    
        // Flash a success message to the session
        return redirect()->route('auction.show', ['id' => $review->auction])->with('success', 'The review was made successfully!');
    }

    public function allReviews($pageNr)
    {
        // Assuming you want to paginate the reviews with a certain number per page (e.g., 10 reviews per page)
        $perPage = 10;
    
        // Retrieve the reviews for the specified page number
        $reviews = Review::paginate($perPage, ['*'], 'page', $pageNr);
    
        return view('pages.reviews_admin', compact('reviews'));
    }
    

    /**
     * Display the specified resource.
     */
    public function show(Review $review)
    {
        //
    }

    /**
     * Show the form for editing the specified resource.
     */
    public function edit(Review $review)
    {
        //
    }

    /**
     * Update the specified resource in storage.
     */
    public function update(Request $request, Review $review)
    {
        //
    }

    /**
     * Remove the specified resource from storage.
     */
    public function destroy(Review $review)
    {
        //
    }
}
