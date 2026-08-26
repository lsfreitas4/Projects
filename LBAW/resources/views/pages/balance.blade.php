@extends('layouts.app')

@section('title', 'Add Funds')

@section('content')
    <div class="add-funds-container">
        <h2 class="add-funds-title">Add Funds</h2>

        <form method="POST" action="{{ route('balance', ['id' => $user->id]) }}" class="add-funds-form">
            @csrf

            <div class="form-group">
                <label for="amount" class="form-label">Amount to Add:</label>
                <input id="amount" type="number" step="0.5" name="amount" min="1" max="1000000" required class="form-input">
                @error('amount')
                <span class="error">{{ $message }}</span>
                @enderror
            </div>

            <button type="submit" class="add-funds-button">Add Funds</button>
            <a href="{{ route('show', ['id' => Auth::user()->id]) }}" class="cancel-button">Cancel</a>
        </form>
    </div>
@endsection
