<?php

namespace App\Http\Controllers;

use App\Models\ReportAuction;
use Illuminate\Http\Request;

use App\Models\AuthenticatedUser;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Auth;
use App\Policies\AuthenticatedUserPolicy;
use App\Http\Controllers\Controller;
use App\Models\Auction;
use App\Models\Bid;
use Illuminate\Support\Facades\DB;

class ReportAuctionController extends Controller
{
    /**
     * Display a listing of the resource.
     */
    public function index($pageNr = 1)
    {
        $userId = Auth::id();
    
        $perPage = 5; // Number of transactions per page

        // Retrieve reports and order them by most recent
        // Assume you want to paginate reports for a specific user or all users
        $reports = ReportAuction::with(['authenticatedUser', 'auctions'])
            ->orderBy('creation_date', 'desc')
            ->paginate($perPage, ['*'], 'page', $pageNr); 

        return view('pages.reports', compact('reports', 'pageNr'));
    }

    /**
     * Show the form for creating a new resource.
     */
    /*public function create()
    {
        return view('pages.reportCreate');
    }*/

    /**
     * Store a newly created resource in storage.
     */
    public function store(Request $request, $auctionId)
{
    try {
        if (!Auth::check()) {
            // Handle unauthenticated user scenario
            return redirect()->route('login'); // Redirect to login or another appropriate response
        }

        $user = Auth::user();

        // Validation rules for the report
        $validatedData = $request->validate([
            'reason' => 'required|string|max:1023',
        ]);

        // Find the auction based on the provided auctionId
        $auction = Auction::find($auctionId);

        if (!$auction) {
            return redirect()->back()->with('error', 'Auction not found.');
        }

        // Create a new report and associate it with the authenticated user and the auction
        $report = new ReportAuction($validatedData);
        $report->authenticatedUser()->associate($user);
        $report->auctions()->associate($auction);
        $report->save();

        return redirect()->route('auction.show', ['id' => $auctionId])->with('success', 'Report submitted successfully.');
    } catch (\Illuminate\Database\QueryException $ex) {
        // Handle database query exceptions
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

   

    /**
     * Display the specified resource.
     */
    
    

    /**
     * Show the form for editing the specified resource.
     */
    public function edit(ReportAuction $reportAuction)
    {
        //
    }

    /**
     * Update the specified resource in storage.
     */
    public function update(Request $request, ReportAuction $reportAuction)
    {
        //
    }

    /**
     * Remove the specified resource from storage.
     */
    public function destroy(ReportAuction $reportAuction)
    {
        //
    }
}
