<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Foundation\Auth\User as Authenticatable;
use Illuminate\Notifications\Notifiable;
use Laravel\Sanctum\HasApiTokens;

class AuthenticatedUser extends Authenticatable
{
    use HasApiTokens, HasFactory, Notifiable;

    protected $table = 'authenticated_user';

    public $timestamps = false;

    protected $fillable = [
        'username',
        'first_name',
        'last_name',
        'email',
        'rating',
        'picture',
        'balance',
        'is_blocked',
        'role',
        'password'
    ];

    protected $hidden = [
        'password'
    ];

    protected $casts = [
        'rating' => 'float',
        'balance' => 'float', 
        'is_blocked' => 'boolean',
    ];

    
    public function addresses() {
        return $this->hasMany(Address::class, 'user');
    }
    
    public function bids() {
        return $this->hasMany(Bid::class, 'user');
    }

    public function transactions() {
        return $this->hasMany(Transaction::class, 'user');
    }

    public function reviewsWritten() {
        return $this->hasMany(Review::class, 'reviewer');
    }
    
    public function reviewsReceived() {
        return $this->hasMany(Review::class, 'reviewed');
    }

    public function notifications() {
        return $this->belongsToMany(Notification::class, 'notification_user', 'user', 'notification');
    }

    public function reportAuctions() {
        return $this->hasMany(ReportAuction::class, 'user');
    }

    public function auctions() {
        return $this->hasMany(Auction::class, 'owner');
    }

    public function followingAuctions() {
        return $this->belongsToMany(Auction::class, 'following', 'user', 'auction')
                    ->withPivot('notifications', 'start_date');
    }

    
}
