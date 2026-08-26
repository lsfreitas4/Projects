@extends('layouts.app')

@section('title', 'My Bids')

@section('content')
    <div class="my-bids-container">
        <h2 class="page-title">My Bids</h2>

        @if ($bids->isEmpty())
            <p class="no-bids-message">No bids found.</p>
        @else
            <div class="bids-list">
                @foreach ($bids as $bid)
                    <div class="bid-card">
                        <p><strong>Auction Title:</strong> {{ $bid->auctions->title }}</p>
                        <p><strong>Value:</strong> {{ $bid->value }}€</p>
                        <p><strong>Creation Date:</strong> {{ $bid->creation_date }}</p>
                    </div>
                @endforeach
            </div>
        @endif

        <div class="pagination">
        @if ($bids->currentPage() > 1)
            <a href="{{ route('myBids', ['id' => $id, 'pageNr' => $bids->currentPage() - 1]) }}" class="prev">Previous</a>
        @endif

        @for ($i = 1; $i <= $bids->lastPage(); $i++)
            <a href="{{ route('myBids', ['id' => $id, 'pageNr' => $i]) }}" class="{{ $i == $bids->currentPage() ? 'active' : '' }}">{{ $i }}</a>
        @endfor

        @if ($bids->currentPage() < $bids->lastPage())
            <a href="{{ route('myBids', ['id' => $id, 'pageNr' => $bids->currentPage() + 1]) }}" class="next">Next</a>
        @endif
    </div>

        <a href="{{ route('show', ['id' => Auth::user()->id]) }}" class="button back-button">Back to profile</a>
    </div>
@endsection
