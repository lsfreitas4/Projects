<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Item extends Model
{
    use HasFactory;

    protected $table = 'item';

    public $timestamps = false;

    protected $fillable = [
        'name', 
        'category', 
        'brand', 
        'color', 
        'picture', 
        'condition'
    ];

    protected $casts = [
        'condition' => 'string'
    ];

    public function auction() {
        return $this->hasOne(Auction::class, 'item'); 
    }
}
