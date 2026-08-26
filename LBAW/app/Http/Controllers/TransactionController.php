<?php

namespace App\Http\Controllers;

use App\Models\Transaction;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;

class TransactionController extends Controller
{
    /**
     * Display a listing of the resource.
     */
    public function index($id, $pageNr = 1)
    {
        $userId = Auth::id();
    
        $perPage = 5; // Number of transactions per page
    
        // Retrieve all transactions ordered by most recent transaction_date
        $transactions = Transaction::where('user', $userId)
            ->orderBy('transaction_date', 'desc')
            ->paginate($perPage, ['*'], 'page', $pageNr);
    
        return view('pages.transactions', compact('transactions', 'pageNr'));
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
    public function show(Transaction $transaction)
    {
        //
    }

    /**
     * Show the form for editing the specified resource.
     */
    public function edit(Transaction $transaction)
    {
        //
    }

    /**
     * Update the specified resource in storage.
     */
    public function update(Request $request, Transaction $transaction)
    {
        //
    }

    /**
     * Remove the specified resource from storage.
     */
    public function destroy(Transaction $transaction)
    {
        //
    }
}
