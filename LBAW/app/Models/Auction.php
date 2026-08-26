<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Auction extends Model
{
    use HasFactory;

    protected $table = 'auction';

    public $timestamps = false;

    protected $fillable = [
        'title', 
        'description', 
        'start_date', 
        'end_date', 
        'starting_price', 
        'current_price', 
        'status'
    ];

    protected $casts = [
        'starting_price' => 'float',
        'current_price' => 'float',
        'status' => 'string',
        'start_date' => 'datetime', 
        'end_date' => 'datetime' 
    ];

    public function items() {
        return $this->belongsTo(Item::class, 'item');
    }

    public function review() {
        return $this->hasOne(Review::class, 'auction');
    }

    public function bids() {
        return $this->hasMany(Bid::class, 'auction');
    }

    public function reports() {
        return $this->hasMany(ReportAuction::class, 'auction');
    }

    public function authenticatedUser() {
        return $this->belongsTo(AuthenticatedUser::class, 'owner');
    }

    public function followers() {
        return $this->belongsToMany(AuthenticatedUser::class, 'following', 'auction', 'user')
                    ->withPivot('notifications', 'start_date');
    }   
}
