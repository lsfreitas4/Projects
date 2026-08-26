<?php

namespace App\Policies;

use App\Models\AuthenticatedUser;

class AuthenticatedUserPolicy
{
    public function viewProfile(AuthenticatedUser $user)
{
    if (!$user) {
        return false; // User is not authenticated
    }
    return true;
}
}
