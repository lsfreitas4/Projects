@extends('layouts.app')

@section('title', 'Notifications')

@section('content')
<div class="container">
    <h1>Notifications</h1>
    <form method="post" action="{{ route('mark_notifications_read') }}">
        @csrf
        <table class="table custom-table"> <!-- Apply a custom class to the table -->
            <thead>
                <tr>
                    <th>Message</th>
                    <th>Type</th>
                    <th>Date</th>
                    <th>Read</th>
                    <th>Mark as Read</th>
                </tr>
            </thead>
            <tbody>
                @foreach ($notifications as $notification)
                <tr class="{{ $notification->read ? 'read-notification' : '' }}">
                    <td>{{ $notification->message }}</td>
                    <td>{{ $notification->type }}</td>
                    <td>{{ $notification->creation_date }}</td>
                    <td>{{ $notification->read ? 'Yes' : 'No' }}</td>
                    <td>
                        @if (!$notification->read)
                        <input type="checkbox" name="notification_ids[]" value="{{ $notification->id }}" class="checkbox-unread">
                        @endif
                    </td>
                </tr>
                @endforeach
            </tbody>
        </table>
        <button type="submit" class="btn btn-primary">Mark as Read</button>
    </form>
    <div class="pagination">
        @if ($notifications->currentPage() > 1)
            <a href="{{ route('notifications.user', ['id' => Auth::id(), 'pageNr' => $notifications->currentPage() - 1]) }}" class="prev">Previous</a>
        @endif

        @for ($i = 1; $i <= $notifications->lastPage(); $i++)
            <a href="{{ route('notifications.user', ['id' => Auth::id(), 'pageNr' => $i]) }}" class="{{ $i == $notifications->currentPage() ? 'active' : '' }}">{{ $i }}</a>
        @endfor

        @if ($notifications->currentPage() < $notifications->lastPage())
            <a href="{{ route('notifications.user', ['id' => Auth::id(), 'pageNr' => $notifications->currentPage() + 1]) }}" class="next">Next</a>
        @endif
    </div>
</div>
@endsection
