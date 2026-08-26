@extends('layouts.app')

@section('title', 'Edit Auction')

@section('content')
    <div class="edit-auction-container">
        <h2 class="edit-auction-title">Edit Auction</h2>
        
        <form action="{{ route('auction.update', $auction->id) }}" method="POST" class="edit-auction-form">
            @csrf
            @method('POST')
            
            <div class="form-group">
                <label for="title" class="form-label">Title:</label>
                <input type="text" id="title" name="title" placeholder="Title" value="{{ $auction->title }}" class="form-input">
            </div>

            <div class="form-group">
                <label for="description" class="form-label">Description:</label>
                <textarea id="description" name="description" placeholder="Description" class="form-textarea">{{ $auction->description }}</textarea>
            </div>

            <button type="submit" class="update-auction-button">Update Auction</button>
            <a class="cancel-button" href="{{ route('auction.show', ['id' => $auction->id]) }}">Cancel</a>
        </form>
    </div>
@endsection
