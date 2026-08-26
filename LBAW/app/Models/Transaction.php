<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Transaction extends Model
{
    use HasFactory;

    protected $table = 'transaction';

    public $timestamps = false;

    protected $fillable = [
        'value', 
        'transaction_date', 
        'description', 
    ];

    protected $casts = [
        'value' => 'float', 
        'transaction_date' => 'datetime'
    ];

    public function authenticatedUser() {
        return $this->belongsTo(AuthenticatedUser::class, 'user');
    }
}
