<?php

namespace App\Http\Controllers;

use App\Models\Notification;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;

class NotificationController extends Controller
{
    /**
     * Display a listing of the resource.
     */
    public function showNotifications($id, $pageNr = 1)
    {
        // Get the authenticated user's ID
        $userId = Auth::id();
        
        $perPage = 20; // Number of transactions per page

        // Retrieve notifications for the authenticated user and order them by most recent
        $notifications = Notification::where('user', $userId)
            ->orderBy('creation_date', 'desc')
            ->paginate($perPage, ['*'], 'page', $pageNr); 
    
        return view('pages.notifications', compact('notifications', 'pageNr'));
    }    

    public function markAsRead(Request $request)
    {
        // Get the IDs of the selected notifications to mark as read
        $notificationIds = $request->input('notification_ids', []);
    
        // Store the current page number in a variable
        $currentPage = $request->input('pageNr', 1);
    
        // Mark the selected notifications as read (update your Notification model as needed)
        Notification::whereIn('id', $notificationIds)->update(['read' => true]);
    
        // Redirect back to the notifications page with the current page number
        return redirect()->route('notifications.user', ['id' => Auth::id(), 'pageNr' => $currentPage]);
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
    public function show(Notification $notification)
    {
        //
    }

    /**
     * Show the form for editing the specified resource.
     */
    public function edit(Notification $notification)
    {
        //
    }

    /**
     * Update the specified resource in storage.
     */
    public function update(Request $request, Notification $notification)
    {
        //
    }

    /**
     * Remove the specified resource from storage.
     */
    public function destroy(Notification $notification)
    {
        //
    }
}
