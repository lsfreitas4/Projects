<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Bid extends Model
{
    use HasFactory;

    protected $table = 'bid';

    public $timestamps = false;

    protected $fillable = [
        'value', 
        'creation_date',
    ];

    protected $casts = [
        'value' => 'float',
        'creation_date' => 'datetime'
    ];

    public function authenticatedUser() {
        return $this->belongsTo(AuthenticatedUser::class, 'user');
    }

    public function auctions() {
        return $this->belongsTo(Auction::class, 'auction');
    }  
}
