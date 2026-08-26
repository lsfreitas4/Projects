@extends('layouts.app')

@section('content')
<div class="profile-update-container">
    <form method="POST" action="{{ route('update', ['id' => $user->id]) }}" enctype="multipart/form-data" class="profile-update-form">
        @csrf
        @method('POST')

        <div class="form-group">
            <label for="picture">Profile Picture</label>
            <input type="file" id="picture" name="picture" accept="image/*">
        </div>

        <div class="form-group">
            <label for="username" class="form-label">Username</label>
            <input id="username" type="text" name="username" value="{{ old('username', $user->username) }}" required autofocus class="form-input">
            @error('username')
            <span class="error">{{ $message }}</span>
            @enderror
        </div>

        <div class="form-group">
            <label for="first_name" class="form-label">First Name</label>
            <input id="first_name" type="text" name="first_name" value="{{ old('first_name', $user->first_name) }}" required class="form-input">
            @error('first_name')
            <span class="error">{{ $message }}</span>
            @enderror
        </div>

        <div class="form-group">
            <label for="last_name" class="form-label">Last Name</label>
            <input id="last_name" type="text" name="last_name" value="{{ old('last_name', $user->last_name) }}" required class="form-input">
            @error('last_name')
            <span class="error">{{ $message }}</span>
            @enderror
        </div>

        <div class="form-group">
            <label for="email" class="form-label">E-Mail Address</label>
            <input id="email" type="email" name="email" value="{{ old('email', $user->email) }}" required class="form-input">
            @error('email')
            <span class="error">{{ $message }}</span>
            @enderror
        </div>

        <div class="form-group">
            <label for="password" class="form-label">New Password (optional)</label>
            <input id="password" type="password" name="password" class="form-input">
            @error('password')
            <span class="error">{{ $message }}</span>
            @enderror
        </div>

        <div class="form-group">
            <label for="password_confirmation" class="form-label">Confirm New Password (optional)</label>
            <input id="password_confirmation" type="password" name="password_confirmation" class="form-input">
        </div>

        <button type="submit" class="update-profile-button">
            Update Profile
        </button>
        <a class="cancel-button" href="{{ route('show', ['id' => $user->id]) }}">Cancel</a>
    </form>
</div>
@endsection
