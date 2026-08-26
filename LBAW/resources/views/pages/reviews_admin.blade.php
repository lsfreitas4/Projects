@extends('layouts.app')

@section('title', 'All Reviews')

@section('content')
<div class="container">
    <h1>All Reviews</h1>

    <table class="table">
        <thead>
            <tr>
                <th>Reviewer</th>
                <th>Reviewed User</th>
                <th>Rating</th>
                <th>Title</th>
                <th>Description</th>
                <th>Date</th>
            </tr>
        </thead>
        <tbody>
            @foreach ($reviews as $review)
                <tr>
                    <td>{{ $review->reviewers->username }}</td>
                    <td>{{ $review->revieweds->username }}</td>
                    <td>{{ $review->rating }}</td>
                    <td>{{ $review->title }}</td>
                    <td>{{ $review->description }}</td>
                    <td>{{ $review->date }}</td>
                </tr>
            @endforeach
        </tbody>
    </table>

    <div class="pagination">
        @if ($reviews->currentPage() > 1)
            <a href="{{ route('reviews.user', ['pageNr' => $reviews->currentPage() - 1]) }}" class="prev">Previous</a>
        @endif

        @for ($i = 1; $i <= $reviews->lastPage(); $i++)
            <a href="{{ route('reviews.user', ['pageNr' => $i]) }}" class="{{ $i == $reviews->currentPage() ? 'active' : '' }}">{{ $i }}</a>
        @endfor

        @if ($reviews->currentPage() < $reviews->lastPage())
            <a href="{{ route('reviews.user', ['pageNr' => $reviews->currentPage() + 1]) }}" class="next">Next</a>
        @endif
    </div>
</div>
@endsection
