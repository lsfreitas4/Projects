@extends('layouts.app')

@section('title', 'Auctions You Are Following')

@section('content')
    <div class="following-auctions-container">
        <h2 class="auctions-title">Auctions You Are Following</h2>

        @if ($followingAuctions->isEmpty())
            <p class="no-auctions-message">You are not following any auctions at the moment.</p>
        @else
            <div class="auctions-list">
                @foreach ($followingAuctions as $auction)
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
            @if ($followingAuctions->currentPage() > 1)
                <a href="{{ route('following.auctions', ['pageNr' => $followingAuctions->currentPage() - 1]) }}" class="prev">Previous</a>
            @endif

            @for ($i = 1; $i <= $followingAuctions->lastPage(); $i++)
                <a href="{{ route('following.auctions', ['pageNr' => $i]) }}" class="{{ $i == $followingAuctions->currentPage() ? 'active' : '' }}">{{ $i }}</a>
            @endfor

            @if ($followingAuctions->currentPage() < $followingAuctions->lastPage())
                <a href="{{ route('following.auctions', ['pageNr' => $followingAuctions->currentPage() + 1]) }}" class="next">Next</a>
            @endif
        </div>

        <a href="{{ route('show', ['id' => Auth::user()->id]) }}" class="button back-button">Back to profile</a>
    </div>
@endsection
