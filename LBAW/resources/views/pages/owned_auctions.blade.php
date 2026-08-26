@extends('layouts.app')

@section('title', 'Your Owned Auctions')

@section('content')
    <div class="owned-auctions-container">
        <h2 class="auctions-title">Your Owned Auctions</h2>

        @if ($ownedAuctions->isEmpty())
            <p class="no-auctions-message">No auctions</p>
        @else
            <div class="auctions-list">
                @foreach ($ownedAuctions as $auction)
                    <div class="auction-card">
                        <a href="{{ route('auction.show', ['id' => $auction->id]) }}" class="auction-link">
                            {{ $auction->title }}
                        </a>
                        <div class="auction-details">
                            <p><strong>Title:</strong> {{ $auction->title }}</p>
                            <p><strong>Description:</strong> {{ $auction->description }}</p>
                            <p><strong>Current Price:</strong> {{ $auction->current_price }}</p>
                            <p><strong>Status:</strong> {{ $auction->status }}</p>
                        </div>
                    </div>
                @endforeach
            </div>
        @endif

        <div class="pagination">
        @if ($ownedAuctions->currentPage() > 1)
            <a href="{{ route('owned.auctions', ['id' => Auth::id(), 'pageNr' => $ownedAuctions->currentPage() - 1]) }}" class="prev">Previous</a>
        @endif

        @for ($i = 1; $i <= $ownedAuctions->lastPage(); $i++)
            <a href="{{ route('owned.auctions', ['id' => Auth::id(), 'pageNr' => $i]) }}" class="{{ $i == $ownedAuctions->currentPage() ? 'active' : '' }}">{{ $i }}</a>
        @endfor

        @if ($ownedAuctions->currentPage() < $ownedAuctions->lastPage())
            <a href="{{ route('owned.auctions', ['id' => Auth::id(), 'pageNr' => $ownedAuctions->currentPage() + 1]) }}" class="next">Next</a>
        @endif
        </div>

        <a href="{{ route('show', ['id' => Auth::user()->id]) }}" class="button back-button">Back to profile</a>
    </div>
@endsection
