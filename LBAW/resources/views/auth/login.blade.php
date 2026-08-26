@extends('layouts.app')

@section('content')
<form method="POST" action="{{ route('login') }}">
    {{ csrf_field() }}

    <label for="email">E-mail</label>
    <input id="email" type="email" name="email" value="{{ old('email') }}" required autofocus></p>
    @if ($errors->has('email'))
        <span class="error">
          {{ $errors->first('email') }}
        </span>
    @endif

    <label for="password">Password</label>
    <input id="password" type="password" name="password" required>
    @if ($errors->has('password'))
        <span class="error">
            {{ $errors->first('password') }}
        </span>
    @endif

    <button type="submit">
        Let's Go!
    </button>
</form>

<button type="button" onclick="redirectToHome()">
    Continue as Guest
</button>

@if (session('message'))
    <p class="message">
        {{ session('message') }}
    </p>
@endif

<script>
    function redirectToHome() {
        window.location.href = "{{ route('home') }}";
    }
</script>
@endsection
