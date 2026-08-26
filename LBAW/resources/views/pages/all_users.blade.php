@extends('layouts.app')

@section('title', 'All Users')

@section('content')
    <h2 class="users-title">All Users</h2>

    <form method="GET" action="{{ route('show.users', ['pageNr' => 1]) }}" id="search-form" class="user-search-form">
        <input type="text" name="query" placeholder="Search users..." value="{{ $query }}" class="search-input">
        <button type="submit" class="search-button">Search</button>
    </form> 

    @if ($users->isEmpty())
        <p class="no-users-message">No users found.</p>
    @else
        <div class="users-container">
            @foreach ($users as $user)
                <div class="user-card">
                    <a href="{{ route('show', ['id' => $user->id]) }}" class="user-name">
                        <strong>{{ $user->username }}</strong>
                    </a>
                    <div class="user-details">
                        @if (isset($user->picture) && $user->picture != 'default.jpg')
                            <img src="{{ asset('storage/' . $user->picture) }}" alt="{{ $user->username }}'s Profile Picture" class="user-profile-image">
                        @else
                            <img src="{{ asset('storage/pictures/default.jpg') }}" alt="Default Profile Picture" class="user-profile-image">
                        @endif
                        <p><strong>First Name:</strong> {{ $user->first_name }}</p>
                        <p><strong>Last Name:</strong> {{ $user->last_name }}</p>
                        <p><strong>Email:</strong> {{ $user->email }}</p>
                        <p><strong>Rating:</strong> {{ $user->rating ?? 'No rating yet' }}</p>
                        @if(Auth::user() && Auth::user()->role === 'ADMIN')
                        <p><strong>Role:</strong> {{ $user->role }}</p>
                        @endif
                    </div>
                </div>
            @endforeach
        </div>

        <div class="pagination">
            <!-- Pagination links updated -->
            @if ($users->currentPage() > 1)
                <a href="{{ route('show.users', ['pageNr' => $users->currentPage() - 1]) }}?query={{ $query }}" class="prev">Previous</a>
            @endif

            @for ($i = 1; $i <= $users->lastPage(); $i++)
                <a href="{{ route('show.users', ['pageNr' => $i]) }}?query={{ $query }}" class="{{ $i == $users->currentPage() ? 'active' : '' }}">{{ $i }}</a>
            @endfor

            @if ($users->currentPage() < $users->lastPage())
                <a href="{{ route('show.users', ['pageNr' => $users->currentPage() + 1]) }}?query={{ $query }}" class="next">Next</a>
            @endif
        </div>
    @endif
@endsection