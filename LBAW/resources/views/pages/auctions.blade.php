@extends('layouts.app')

@section('title', 'Auctions')

@section('content')
    <h2 class="auctions-title">All Auctions</h2>

    <form method="GET" action="{{ route('auction.index', ['pageNr' => 1]) }}" id="search-form" class="auction-search-form">
        <input type="text" name="query" placeholder="Search auctions..." value="{{ $query }}" class="search-input">
        <button type="submit" class="search-button">Search</button>
    </form>

    <div class="filter-section">

        <div class="filter-price">
            <h3>Price</h3>
            <form method="GET" action="{{ route('auction.index', ['pageNr' => 1]) }}" id="filter-form">
                <div class="price-inputs">
                    <div class="input-group">
                        <input type="number" name="min-price" id="min-price" value="{{ $minPrice }}" placeholder="Min price..." min="0">
                    </div>
                    <div class="input-group">
                        <input type="number" name="max-price" id="max-price" value="{{ $maxPrice }}" placeholder="Max price...">
                    </div>
                </div>
        </div>

        <div class="filter-condition">
            <h3>Condition</h3>
                <select name="condition" id="condition">
                    <option value="">Select Condition</option>
                    <option value="NEW" @if ($condition === 'NEW') selected @endif>NEW</option>
                    <option value="LIKE NEW" @if ($condition === 'LIKE NEW') selected @endif>LIKE NEW</option>
                    <option value="GOOD" @if ($condition === 'GOOD') selected @endif>GOOD</option>
                    <option value="USED" @if ($condition === 'USED') selected @endif>USED</option>
                    <option value="EXCELLENT" @if ($condition === 'EXCELLENT') selected @endif>EXCELLENT</option>
                </select>
        </div>

        <div class="filter-category">
            <h3>Category</h3>
            <input type="text" name="category" placeholder="Search by Category..." value="{{ $category }}" class="search-input">
        </div>

        <div class="order-by">
            <h3>Sort</h3>
                <select name="sort">
                    <option value="closest" {{ $sort == 'closest' ? 'selected' : '' }}>Closest Ending</option>
                    <option value="price_asc" {{ $sort == 'price_asc' ? 'selected' : '' }}>Price Low to High</option>
                    <option value="price_desc" {{ $sort == 'price_desc' ? 'selected' : '' }}>Price High to Low</option>
                    <option value="alpha_asc" {{ $sort == 'alpha_asc' ? 'selected' : '' }}>A-Z</option>
                    <option value="alpha_desc" {{ $sort == 'alpha_desc' ? 'selected' : '' }}>Z-A</option>
                </select>
        </div>

        <button type="submit" class="apply-filters">Apply Filters</button>

            </form>
    </div>

    @if ($auctions->isEmpty())
        <p class="no-auctions-message">No auctions found.</p>
    @else
    <div class="auctions-container">
            @foreach ($auctions as $auction)
                <div class="auction-card">
                    <a href="{{ route('auction.show', ['id' => $auction->id]) }}" class="auction-title">
                        {{ $auction->title }}
                    </a>
                    <div class="auction-details">
                        @if (isset($auction->items->picture))
                            <div class="auction-image">
                                <img src="{{ asset('storage/' . $auction->items->picture) }}" alt="Auction Image" class="auction-image">
                            </div>
                        @endif
                        <p><strong>Title:</strong> {{ $auction->title }}</p>
                        <p><strong>Description:</strong> {{ $auction->description }}</p>
                        <p><strong>Current Price:</strong> {{ $auction->current_price }}€</p>
                        <p><strong>Status:</strong> {{ $auction->status }}</p>
                        <p id="time-remaining-{{ $auction->id }}">
                            <strong>Time Left:</strong> 
                            <span id="days-{{ $auction->id }}"></span>d 
                            <span id="hours-{{ $auction->id }}"></span>h 
                            <span id="minutes-{{ $auction->id }}"></span>m 
                            <span id="seconds-{{ $auction->id }}"></span>s
                        </p>
                    </div>
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
                </div>
            @endforeach
        </div>

        <div class="pagination">
            @if ($auctions->currentPage() > 1)
                <a href="{{ route('auction.index', ['pageNr' => $auctions->currentPage() - 1, 'query' => $query, 'min-price' => $minPrice, 'max-price' => $maxPrice, 'condition' => $condition, 'category' => $category, 'sort' => $sort]) }}" class="prev">Previous</a>
            @endif

            @for ($i = 1; $i <= $auctions->lastPage(); $i++)
                <a href="{{ route('auction.index', ['pageNr' => $i, 'query' => $query, 'min-price' => $minPrice, 'max-price' => $maxPrice, 'condition' => $condition, 'category' => $category, 'sort' => $sort]) }}" class="{{ $i == $auctions->currentPage() ? 'active' : '' }}">{{ $i }}</a>
            @endfor

            @if ($auctions->currentPage() < $auctions->lastPage())
                <a href="{{ route('auction.index', ['pageNr' => $auctions->currentPage() + 1, 'query' => $query, 'min-price' => $minPrice, 'max-price' => $maxPrice, 'condition' => $condition, 'category' => $category, 'sort' => $sort]) }}" class="next">Next</a>
            @endif
        </div>
    @endif
@endsection
