@extends('layouts.app')

@section('title', 'Create Auction')

@section('content')
    <div class="create-auction-container">
        <h2 class="form-title">Create Auction</h2>
        
        <form action="{{ route('auction.store') }}" method="POST" class="auction-form" enctype="multipart/form-data">
            @csrf

            <!-- Display Error Messages -->
            @if ($errors->any())
                <div class="alert alert-danger">
                    <ul>
                        @foreach ($errors->all() as $error)
                            <li>{{ $error }}</li>
                        @endforeach
                    </ul>
                </div>
            @endif

            <!-- Auction Fields -->
            <label for="title">Title:</label>
            <input type="text" id="title" name="title" placeholder="Title" value="{{ old('title') }}">

            <label for="description">Description:</label>
            <textarea id="description" name="description" placeholder="Description">{{ old('description') }}</textarea>

            <label for="picture">Item Picture</label>
            <input type="file" id="picture" name="picture" accept="image/*">

            <label for="end_date">End Date:</label>
            <input type="datetime-local" id="end_date" name="end_date" value="{{ old('end_date', now()->addDays(7)->addSeconds(1)->toDateTimeLocalString()) }}" min="{{ now()->addDays(7)->addSeconds(1)->toDateTimeLocalString() }}" max="{{ now()->addDays(30)->toDateTimeLocalString() }}">

            <label for="starting_price">Starting Price:</label>
            <input type="number" id="starting_price" name="starting_price" placeholder="Starting Price" value="{{ old('starting_price') }}" min="1" max="10000000">

            <!-- Item Fields -->
            <label for="name">Item Name:</label>
            <input type="text" id="name" name="name" placeholder="Item Name" value="{{ old('name') }}">

            <label for="category">Category:</label>
            <input type="text" id="category" name="category" placeholder="Category" value="{{ old('category') }}">

            <label for="brand">Brand:</label>
            <input type="text" id="brand" name="brand" placeholder="Brand" value="{{ old('brand') }}">

            <label for="color">Color:</label>
            <input type="text" id="color" name="color" placeholder="Color" value="{{ old('color') }}">

            <label for="condition">Condition:</label>
            <select id="condition" name="condition">
                <option value="">Select Condition</option>
                <option value="NEW" {{ old('condition') == 'NEW' ? 'selected' : '' }}>NEW</option>
                <option value="LIKE NEW" {{ old('condition') == 'LIKE NEW' ? 'selected' : '' }}>LIKE NEW</option>
                <option value="EXCELLENT" {{ old('condition') == 'EXCELLENT' ? 'selected' : '' }}>EXCELLENT</option>
                <option value="GOOD" {{ old('condition') == 'GOOD' ? 'selected' : '' }}>GOOD</option>
                <option value="USED" {{ old('condition') == 'USED' ? 'selected' : '' }}>USED</option>
            </select>
            <button type="submit" class="submit-button">Create Auction</button>
            <a href="{{ url('/home') }}" class="cancel-button">Cancel</a>
        </form>
    </div>
@endsection
