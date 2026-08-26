<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class ReportAuction extends Model
{
    use HasFactory;

    protected $table = 'report_auction';
    public $timestamps = false;

    protected $fillable = [
        'reason',
        'creation_date',
    ];

    protected $casts = [
        'creation_date' => 'datetime'
    ];

    public function auctions() {
        return $this->belongsTo(Auction::class, 'auction');  
    }

    public function authenticatedUser() {
        return $this->belongsTo(AuthenticatedUser::class, 'user');  
    }
}
