@extends('layouts.app')

@section('title', 'Profile')

@section('content')
    <div class="profile-container">
        <h1 class="profile-header">User Information</h1>
        
        <div class="user-info">
            <div class="profile-picture">
                @if ($user->picture == 'default.jpg')
                    <img src="{{ asset('storage/pictures/default.jpg') }}" alt="Default Profile Picture" class="user-profile-image">
                @else
                    <img src="{{ asset('storage/' . $user->picture) }}" alt="{{ $user->username }}'s Profile Picture" class="user-profile-image">
                @endif
            </div>
            <p><strong>Name:</strong> {{ $user->username }}</p>
            <p><strong>Email:</strong> {{ $user->email }}</p>
            <p><strong>First Name:</strong> {{ $user->first_name }}</p>
            <p><strong>Last Name:</strong> {{ $user->last_name }}</p>
            <p><strong>Email:</strong> {{ $user->email }}</p>
            @if(Auth::user()->role != 'ADMIN')
                <p><strong>Rating:</strong> {{ $user->rating ?: 'No rating yet' }}</p>
            @endif
            
            @if(Auth::user()->role != 'ADMIN' && Auth::user()->id == $user->id)
                <p><strong>Balance:</strong> {{ $user->balance }}€</p>
            @endif
            @if($user->is_blocked == TRUE)
                <h4><strong>This user has been blocked!</strong></h4>
            @endif
        </div>

        @if(Auth::check() && Auth::user()->id == $user->id && Auth::user()->role != 'ADMIN')
            <h2 class="addresses-header">{{ $user->first_name }} {{ $user->last_name }} Addresses</h2>
            <div class="addresses-container">
                @foreach ($user->addresses as $address)
                    <div class="address-item">
                        <p>Street: {{ $address->street }}</p>
                        <p>Postal Code: {{ $address->postal_code }}</p>
                        <p>City: {{ $address->city }}</p>
                        <p>Country: {{ $address->country }}</p>
                    </div>
                @endforeach
            </div>
        @endif

        <div class="profile-reviews">
            @if ($user->reviewsReceived->count() > 0)
            <h2>Recent Reviews for {{ $user->username }}</h2>
                <ul class="review-list">
                    @foreach ($user->reviewsReceived->take(3) as $review)
                        <li class="review-item">
                            <h3 class="review-title"><strong>{{ $review->title }}</strong></h3>
                            <div class="review-header">
                                <strong> {{ $review->reviewers->username }}</strong> given <strong>{{ $review->rating }}</strong> stars:
                            </div>
                            <p class="review-description">Description: {{ $review->description }}</p>
                            <p class="review-date"><strong>Date:</strong> {{ $review->date }}</p>
                        </li>
                    @endforeach
                </ul>
            @endif
        </div>

        <div class="action-buttons">
            @if(Auth::user()->role === 'ADMIN' && Auth::user()->id != $user->id && $user->role !== 'ADMIN')
            <a href="{{ route('edit', ['id' => $user->id]) }}" class="button edit-button">Edit User</a>
            <form method="POST" action="{{ route('delete.user', ['id' => $user->id]) }}" class="admin-form">
                @csrf   
                <button type="submit" class="button delete-button">Delete User</button>
            </form>
        @endif

            @if(Auth::user()->role === 'ADMIN' && Auth::user()->id != $user->id && $user->role !== 'ADMIN' && $user->is_blocked == FALSE)
                <form method="POST" action="{{ route('block.user', ['id' => $user->id]) }}" class="admin-form">
                    @csrf
                    <button type="submit" class="button promote-button">Block User</button>
                </form>
            @endif
            @if(AUth::user()->role === 'ADMIN' && Auth::user()->id != $user->id && $user->role !== 'ADMIN' && $user->is_blocked == TRUE)
                <form method="POST" action="{{ route('unblock.user', ['id' => $user->id]) }}" class="admin-form">
                    @csrf 
                    <button type="submit" class="button promote-button">Unblock User</button>
                </form>    
            @endif
            @if(Auth::user()->id == $user->id)
                <a href="{{ route('edit', ['id' => Auth::user()->id]) }}" class="button edit-profile">Edit Profile</a>
                @if(Auth::user()->role != 'ADMIN')
                    <a href="{{ route('balance', ['id' => Auth::user()->id]) }}" class="button add-funds">Add Funds</a>
                    <a href="{{ route('owned.auctions', ['id' => Auth::user()->id, 'pageNr' => 1]) }}" class="button my-auctions">My Auctions</a>
                    <a href="{{ route('myBids', ['id' => Auth::user()->id, 'pageNr' => 1]) }}" class="button my-bids">My Bids</a>
                    <a href="{{ route('transactions.user', ['id' => Auth::user()->id, 'pageNr' => 1]) }}" class="button my-bids">Transaction History</a>
                @endif
                <a href="{{ route('show.users', ['pageNr' => 1]) }}" class="button back-users">Back to Users Page</a>
                <a href="{{ url('/home') }}" class="button back-home">Back to Home Page</a>
            @elseif(Auth::check())
                <a href="{{ route('show.users', ['pageNr' => 1]) }}" class="button back-users">Back to Users Page</a>
                <a href="{{ url('/home') }}" class="button back-home">Back to Home Page</a>
            @endif
            @if(Auth::user()->id == $user->id && Auth::user()->role !== 'ADMIN')
                <form id="delete-form" method="POST" action="{{ route('delete.user', ['id' => $user->id]) }}">
                    @csrf   
                    <button type="button" class="button delete-button" onclick="confirmDelete()">Delete My Account</button>
                </form>

                <script>
                    function confirmDelete() {
                        if (confirm("This action is irreversible. Are you sure you want to delete your account?")) {
                            document.getElementById('delete-form').submit();
                        }
                    }
                </script>
            @endif
        </div>
    </div>
@endsection
