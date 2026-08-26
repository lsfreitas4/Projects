@extends('layouts.app')

@section('content')
<form method="POST" action="{{ route('register') }}">
    {{ csrf_field() }}

    <label for="username">Username</label>
    <input id="username" type="text" name="username" value="{{ old('username') }}" required autofocus>
    @if ($errors->has('username'))
      <span class="error">
          {{ $errors->first('username') }}
      </span>
    @endif

    <label for="first_name">First Name</label>
    <input id="first_name" type="text" name="first_name" value="{{ old('first_name') }}" required>
    @if ($errors->has('first_name'))
      <span class="error">
          {{ $errors->first('first_name') }}
      </span>
    @endif

    <label for="last_name">Last Name</label>
    <input id="last_name" type="text" name="last_name" value="{{ old('last_name') }}" required>
    @if ($errors->has('last_name'))
      <span class="error">
          {{ $errors->first('last_name') }}
      </span>
    @endif

    <label for="email">E-Mail Address</label>
    <input id="email" type="email" name="email" value="{{ old('email') }}" required>
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

    <label for="password-confirm">Confirm Password</label>
    <input id="password-confirm" type="password" name="password_confirmation" required>

    <button type="submit">
      Register
    </button>
</form>
@endsection
