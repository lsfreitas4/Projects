<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Review extends Model
{
    use HasFactory;

    protected $table = 'review';
    public $timestamps = false;

    protected $fillable = [
        'rating',
        'title',
        'description',
        'date'
    ];

    protected $casts = [
        'rating' => 'int',
        'date' => 'datetime'
    ];

    public function auctions() {
        return $this->belongsTo(Auction::class, 'auction');
    }

    public function reviewers() {
        return $this->belongsTo(AuthenticatedUser::class, 'reviewer');
    }

    public function revieweds() {
        return $this->belongsTo(AuthenticatedUser::class, 'reviewed');
    }
}
