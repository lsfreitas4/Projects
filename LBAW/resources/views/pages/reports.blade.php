@extends('layouts.app')

@section('title', 'Reports')

@section('content')
<div class="container">
        <h1>All Reports</h1>
        <table>
            <thead>
                <tr>
                    <th>User</th>
                    <th>Auction Title</th>
                    <th>Creation Date</th>
                    <th>Reason</th>
                </tr>
            </thead>
            <tbody>
                @foreach ($reports as $report)
                    <tr>
                        <td><a href="{{ route('show', ['id' => $report->authenticatedUser->id]) }}">{{ $report->authenticatedUser->username }}</a></td>
                        <td><a href="{{ route('auction.show', ['id' => $report->auctions->id]) }}">{{ $report->auctions->title }}</a></td>
                        <td>{{ $report->creation_date }}</td>
                        <td>{{ $report->reason }}</td>
                    </tr>
                @endforeach
            </tbody>
        </table>

        <div class="pagination">
            @if ($reports->currentPage() > 1)
                <a href="{{ route('reports.user', ['pageNr' => $reports->currentPage() - 1]) }}" class="prev">Previous</a>
            @endif

            @for ($i = 1; $i <= $reports->lastPage(); $i++)
                <a href="{{ route('reports.user', ['pageNr' => $i]) }}" class="{{ $i == $reports->currentPage() ? 'active' : '' }}">{{ $i }}</a>
            @endfor

            @if ($reports->currentPage() < $reports->lastPage())
                <a href="{{ route('reports.user', ['pageNr' => $reports->currentPage() + 1]) }}" class="next">Next</a>
            @endif
        </div>
    </div>
@endsection
