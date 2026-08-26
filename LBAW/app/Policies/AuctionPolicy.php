<?php

namespace App\Policies;

use App\Models\AuthenticatedUser;

class AuctionPolicy
{
    public function viewAuction(AuthenticatedUser $user)
    {
        if (!$user) {
            return false; // User is not authenticated
        }
        return true;
    }
}
