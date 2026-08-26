@extends('layouts.app')

@section('title', 'Create Review')

@section('content')
<div class="container">
    <h1>Create Review for {{ $auction->authenticatedUser->username }} from auction {{ $auction->title }}</h1>
    
    <form method="POST" action="{{ route('reviews.store') }}">
        @csrf
        <input type="hidden" name="auction_id" value="{{ $auction->id }}">
        
        <div class="form-group">
            <label for="rating">Rating:</label>
            <select name="rating" id="rating" class="form-control">
                <option value="1">1 - Poor</option>
                <option value="2">2 - Fair</option>
                <option value="3">3 - Average</option>
                <option value="4">4 - Good</option>
                <option value="5">5 - Excellent</option>
            </select>
        </div>
        
        <div class="form-group">
            <label for="title">Title:</label>
            <input type="text" name="title" id="title" class="form-control" required>
        </div>
        
        <div class="form-group">
            <label for="description">Description:</label>
            <textarea name="description" id="description" class="form-control" rows="4" required></textarea>
        </div>
        
        <button type="submit" class="btn btn-primary">Submit Review</button>
    </form>
</div>
@endsection
