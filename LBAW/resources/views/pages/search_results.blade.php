@extends('layouts.app')

@section('title', 'General Search')

@section('content')
    <section class="search-results-section">
        <h2 class="section-title">Auctions</h2>
        @if ($auctions->count() > 0)
            <div class="cards-container">
                @foreach ($auctions as $auction)
                    <div class="card">
                        <a href="{{ route('auction.show', ['id' => $auction->id]) }}" class="card-title">{{ $auction->title }}</a>
                    </div>
                @endforeach
            </div>
        @else
            <p class="no-results-message">No auctions found.</p>
        @endif
    </section>

    <section class="search-results-section">
        <h2 class="section-title">Users</h2>
        @if ($users->count() > 0)
            <div class="cards-container">
                @foreach ($users as $user)
                    <div class="card">
                        <a href="{{ route('show', ['id' => $user->id]) }}" class="card-title">{{ $user->username }}</a>
                    </div>
                @endforeach
            </div>
        @else
            <p class="no-results-message">No users found.</p>
        @endif
    </section>
@endsection
