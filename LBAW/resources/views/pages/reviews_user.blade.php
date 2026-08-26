@extends('layouts.app')

@section('title', 'Closed Auctions')

@section('content')
<div class="container">
    <h1>Pending Auctions for Review</h1>
    <table class="table">
        <thead>
            <tr>
                <th>Title</th>
                <th>Description</th>
                <th>Start Date</th>
                <th>End Date</th>
                <th>Starting Price</th>
                <th>Current Price</th>
                <th>Owner</th>
            </tr>
        </thead>
        <tbody>
            @foreach ($closedAuctions as $auction)
                <tr>
                    <td><a href="{{ route('reviews.create', ['id' => $auction->id]) }}">{{ $auction->title }}</a></td>
                    <td>{{ $auction->description }}</td>
                    <td>{{ $auction->start_date }}</td>
                    <td>{{ $auction->end_date }}</td>
                    <td>{{ $auction->starting_price }}</td>
                    <td>{{ $auction->current_price }}</td>
                    <td>{{ $auction->authenticatedUser->username }}</td>
                </tr>
            @endforeach
        </tbody>
    </table>
</div>
@endsection
