<!DOCTYPE html>
<html lang="{{ app()->getLocale() }}">
    <head>
        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">

        <!-- CSRF Token -->
        <meta name="csrf-token" content="{{ csrf_token() }}">

        <title>{{ config('app.name', 'Laravel') }}</title>

        <!-- Styles -->
        <link href="{{ url('css/milligram.min.css') }}" rel="stylesheet">
        <link href="{{ url('css/app.css') }}" rel="stylesheet">
        <script type="text/javascript">
            // Fix for Firefox autofocus CSS bug
            // See: http://stackoverflow.com/questions/18943276/html-5-autofocus-messes-up-css-loading/18945951#18945951
        </script>
        <script type="text/javascript" src={{ url('js/app.js') }} defer>
        </script>
    </head>
    <body>
    <main>
        <header>
            <h1><a href="{{ url('/home') }}">StyleSwap!</a></h1>
            @if (!Auth::check())
                <!-- Non-authenticated user links -->
                <a class="button" href="{{ url('/login') }}">Login</a>
                <a class="button" href="{{ route('register', ['pageNr' => 1]) }}">Register</a>
            @endif
            <!-- Search form for all users -->
            @if(Auth::check())
                <form id="search-form" action="{{ route('search.results') }}" method="GET">
                    <input type="text" name="query" placeholder="Search...">
                    <button type="submit">Search</button>
                </form>
            @endif
            <!-- Links visible to all users -->
            <a class="button" href="{{ route('auction.index', ['pageNr' => 1]) }}">Auctions</a>
            <a class="button" href="{{ route('show.users', ['pageNr' => 1]) }}">Users</a>
            @if (Auth::check())
                <!-- Links for authenticated users -->
                <a class="button" href="{{ route('show', ['id' => Auth::user()->id]) }}">My Profile</a>
                <span>{{ Auth::user()->name }}</span>
                <!-- Other authenticated user links -->
                @if(Auth::user()->role != 'ADMIN')
                    <a class="button" href="{{ route('auction.create') }}">Create Auction</a>
                    <a class="button" href="{{ route('following.auctions', ['pageNr' => 1]) }}">Following</a>
                    <a class="button" href="{{ route('reviews.user')}}">Pending Reviews</a>
                @endif
                <a class="button" href="{{ route('notifications.user', ['id' => Auth::user()->id, 'pageNr' => 1])}}">Notifications</a>
                @if(Auth::user()->role === 'ADMIN')
                    <a class="button" href="{{ route('reports.user', ['pageNr' => 1])}}">Reports</a>
                    <a class="button" href="{{ route('reviews.admin', ['pageNr' => 1])}}">Reviews</a>
                @endif
                <a class="button" href="{{ url('/logout') }}">Logout</a>
            @endif
        </header>
        <section id="messages">
            @if(session('success'))
                <div class="alert alert-success">
                    {{ session('success') }}
                </div>
            @endif

            @if(session('error'))
                <div class="alert alert-danger">
                    {{ session('error') }}
                </div>
            @endif
        </section>
        <section id="content">
            @yield('content')
        </section>
    </main>
</body>

<footer id="sticky-footer" class="flex-shrink-0 py-4 bg-dark text-white-50">
    <div class="container text-center">
      <small> <a href="/aboutUs">About Us </a>  | <a href="/contacts">Contacts</a> | <a href="/faq">FAQ</a> | <a href="/features">Features</a></small>
    </div>
  </footer>  

</html>