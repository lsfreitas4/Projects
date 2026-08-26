@extends('layouts.app')

@section('title', 'Home')

@section('content')
    <div class="content-container">
        <div class="auctions-container-home">
            <h1>Top Auctions</h1>
            @foreach ($topAuctions as $auction)
                <div class="auction-item">
                    <h2><a class="buttonColor" href="{{ route('auction.show', ['id' => $auction->id]) }}">{{ $auction->title }}</a></h2>
                    <p>Current Price: {{ $auction->current_price }}€</p>
                    <script>
                        var auctionEndDate{{ $auction->id }} = new Date("{{ $auction->end_date }}");
                        setInterval(function() {
                            var now = new Date();
                            var timeRemaining = auctionEndDate{{ $auction->id }} - now;
                            if (timeRemaining > 0) {
                                var seconds = Math.floor((timeRemaining / 1000) % 60);
                                var minutes = Math.floor((timeRemaining / 1000 / 60) % 60);
                                var hours = Math.floor((timeRemaining / (1000 * 60 * 60)) % 24);
                                var days = Math.floor(timeRemaining / (1000 * 60 * 60 * 24));
                                document.getElementById('days-{{ $auction->id }}').innerText = days;
                                document.getElementById('hours-{{ $auction->id }}').innerText = hours;
                                document.getElementById('minutes-{{ $auction->id }}').innerText = minutes;
                                document.getElementById('seconds-{{ $auction->id }}').innerText = seconds;
                            } else {
                                document.getElementById('time-remaining-{{ $auction->id }}').innerText = "Auction ended";
                            }
                        }, 1000);
                    </script>
                    <p id="time-remaining-{{ $auction->id }}">
                        <strong>
                            <span id="days-{{ $auction->id }}"></span> days
                            <span id="hours-{{ $auction->id }}"></span> hours
                            <span id="minutes-{{ $auction->id }}"></span> minutes
                            <span id="seconds-{{ $auction->id }}"></span> seconds
                        </strong> 
                    </p>
                </div>
            @endforeach
        </div>

        <div class="bidders-container">
            <h1>Top Bidders</h1>
            @foreach ($topBidders as $bidder)
                <div class="bidder-item">
                    <h2><a class="buttonColor" href="{{ route('show', ['id' => $bidder->authenticatedUser->id]) }}">{{ $bidder->authenticatedUser->username }}</a></h2>
                    <p>Total Bids: {{ $bidder->total_bids }}</p>
                    <p>Total Bid Amount: {{ $bidder->total_bid_amount }}€</p>
                </div>
            @endforeach
        </div>
    </div>
@endsection
