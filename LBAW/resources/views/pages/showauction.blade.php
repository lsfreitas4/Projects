@extends('layouts.app')

@section('title', 'Auction Details')

@section('content')
    <div class="auction-details-container">
        
        <h2 class="details-title">Auction Details</h2>

        @if (isset($item->picture) && $item->picture)
            <div class="auction-image">
                <img src="{{ asset('storage/' . $item->picture) }}" alt="Auction Image">
            </div>
        @endif

        <div class="auction-info">
            <p><strong>Title:</strong> {{ $auction->title }}</p>
            <p><strong>Owner:</strong> {{ $auction->authenticatedUser->username}}</p>
            <p><strong>Current Price:</strong> {{ $auction->current_price }}</p>
            <p><strong>Start Date:</strong> {{ $auction->start_date->format('Y-m-d H:i:s') }}</p>
            <p><strong>End Date:</strong> {{ $auction->end_date->format('Y-m-d H:i:s') }}</p>
            <p><strong>Status: {{ $auction->status }}</strong></p>
            <p><strong>Category:</strong> {{ $item->category }}</p>
            <p><strong>Color:</strong> {{ $item->color }}</p>
            <p><strong>Condition:</strong> {{ $item->condition }}</p>
            <p><strong>Brand:</strong> {{ $item->brand }}</p>
            <p><strong>Description:</strong> {{ $auction->description }}</p>
        </div>

        <!-- Action buttons -->
        @if (Auth::check() && Auth::user()->id == $auction->owner && $auction->status == 'ACTIVE')
            <div class="action-buttons">
                <a href="{{ route('auction.edit', $auction->id) }}" class="button edit-button">Edit Auction</a>
                <form method="POST" action="{{ route('auction.cancel', $auction->id) }}" class="cancel-form">
                    @csrf
                    @method('PUT')
                    <button type="submit" class="button cancel-button" onclick="return confirm('Are you sure you want to cancel this auction?')">Cancel Auction</button>
                </form>
            </div>
        @endif
        


        <!-- Back to Auctions button -->
        <a href="{{ route('auction.index', 1) }}" class="button">Back to Auctions</a>
        <a href="{{ url('/home') }}" class="button back-home">Back to Home Page</a>
        <!-- Table to display bid information -->
        <table id="bidTable">
            <thead>
                <tr>
                    <th>Place</th>
                    <th>Rating</th>
                    <th>Username</th>
                    <th>Bid Amount</th>
                    <th>Creation Date</th>
                </tr>
            </thead>
            <tbody id="bidTableBody">
                @forelse ($auction->bids as $bid)
                    <tr>
                        <td>{{ $loop->iteration }}</td>
                        <td>{{ $bid->authenticatedUser->rating ?? '(No rating yet)' }}</td>
                        <td>{{ $bid->authenticatedUser->username ?? '(null)'}}</td>
                        <td>{{ $bid->value }}</td>
                        <td>{{ $bid->creation_date->format('Y-m-d H:i:s') }}</td>
                    </tr>
                @empty
                    <tr>
                        <td colspan="6">There are no bids yet, be the first!</td>
                    </tr>
                @endforelse
            </tbody>
        </table>

        <!-- Bid Form -->
        @if(Auth::user()->role != 'ADMIN' && Auth::user() != $auction->authenticatedUser && $auction->status === 'ACTIVE')
            <form id="bidForm" method="POST" action="/auction/{{ $auction->id }}/bid">
                @csrf
                <label for="bid_amount">Bid Amount:</label>
                <input type="number" id="bid_amount" name="bid_amount" min="{{ $auction->current_price }}" step="1" required>
                <button type="submit" class="button">Bid</button>
            </form>
            @if (!Auth::user()->followingAuctions->contains($auction->id))
                <form action="{{ route('follow.auction', ['auction' => $auction->id]) }}" method="POST">
                    @csrf
                    <button type="submit" class="button">Follow auction</button>
                </form>
            @else
                <form action="{{ route('unfollow.auction', ['auction' => $auction->id]) }}" method="POST">
                    @csrf
                    @method('DELETE') 
                    <button type="submit" class="cancel-button">Unfollow auction</button>
                </form>
            @endif
        @endif
        @if(Auth::check() && Auth::user()->role == 'ADMIN')
            <form method="POST" action="{{ route('auction.delete', $auction->id) }}" class="delete-form">
                @csrf
                @method('DELETE')
                <button type="submit" class="button delete-button" onclick="return confirm('Are you sure you want to delete this auction?')">Delete Auction</button>
            </form>
        @endif
        @if(Auth::user() != $auction->authenticatedUser && Auth::user()->role != 'ADMIN')
            <h3>Report this auction here if you think something is wrong</h3>   
            <form id="reportForm" method="POST" action="/auction/{{ $auction->id }}/report">
                @csrf
                <input type="hidden" name="auction" value="{{ $auction->id }}"> 
                <label for="reason">Reason:</label>
                <input type="text" id="reason" name="reason" required>
                <button type="submit" class="button">Report</button>
            </form>
        @endif

    </div>
@endsection
