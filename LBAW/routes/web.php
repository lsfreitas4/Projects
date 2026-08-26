<?php

use Illuminate\Support\Facades\Route;

use App\Http\Controllers\AuthenticatedUserController;
use App\Http\Controllers\AuctionController;
use App\Http\Controllers\BidController;
use App\Http\Controllers\NotificationController;
use App\Http\Controllers\ReportAuctionController;
use App\Http\Controllers\ReviewController;
use App\Http\Controllers\TransactionController;

use App\Http\Controllers\Auth\LoginController;
use App\Http\Controllers\Auth\RegisterController;

/*
|--------------------------------------------------------------------------
| Web Routes
|--------------------------------------------------------------------------
|
| Here is where you can register web routes for your application. These
| routes are loaded by the RouteServiceProvider and all of them will
| be assigned to the "web" middleware group. Make something great!
|
*/

// Home
Route::redirect('/', '/login');

// Authentication
Route::controller(LoginController::class)->group(function () {
    Route::get('/login', 'showLoginForm')->name('login');
    Route::post('/login', 'authenticate');
    Route::get('/logout', 'logout')->name('logout');
});

Route::controller(RegisterController::class)->group(function () {
    Route::get('/register', 'showRegistrationForm')->name('register');
    Route::post('/register', 'register');
});

Route::controller(AuthenticatedUserController::class)->group(function () {
    Route::get('/home','index')->name('home');
    Route::get('/aboutUs', 'showAboutUs')->name('aboutUs');
    Route::get('/faq', 'showFAQ')->name('faq');
    Route::get('/features', 'showFeatures')->name('features');
    Route::get('/contacts', 'showContacts')->name('contacts');
    Route::get('/profile/{id}', 'show')->name('show');
    Route::get('/profile/{id}/edit', 'edit')->name('edit');
    Route::post('/profile/{id}/edit', 'update')->name('update');
    Route::get('/profile/{id}/balance', 'balance')->name('balance');
    Route::post('/profile/{id}/balance', 'addFunds')->name('addFunds');
    Route::post('/profile/{id}/promote', 'promoteToAdmin')->name('promote.admin');
    Route::get('users/{pageNr}', 'all')->name('show.users');
    Route::get('/auctionCreate', 'showCreateAuction')-> name('showCreateAuction');
    Route::get('/search', 'searchResults')->name('search.results');
    Route::post('/profile/{id}/block/', 'blockUser')->name('block.user');
    Route::post('/profile/{id}/unblock', 'unblockUser')->name('unblock.user');
    Route::post('/profile/{id}/delete', 'deleteUser')->name('delete.user');
});


Route::controller(AuctionController::class)->group(function () {
    Route::get('/auctions/{pageNr}', 'index')->name('auction.index');
    Route::get('/auction/{id}', 'show')->name('auction.show');  
    Route::get('/auctionCreate', 'create')->name('auction.create');
    Route::post('/auctionCreate', 'store')->name('auction.store');
    Route::get('/auction/{id}/edit', 'edit')->name('auction.edit');
    Route::post('/auction/{id}/edit', 'update')->name('auction.update');
    Route::put('/auction/{id}/cancel', 'cancel')->name('auction.cancel');
    Route::get('/profile/{id}/auctions/{pageNr}', 'showOwnedAuctions')->name('owned.auctions');
    Route::get('/auction/{id}',  'show')->name('auction.show');
    Route::post('/auction/{auction}/follow', 'follow')->name('follow.auction');
    Route::delete('/auction/{auction}/unfollow', 'unfollow')->name('unfollow.auction');    
    Route::get('/following/{pageNr}', 'following')->name('following.auctions');
    Route::delete('/auction/{id}/delete', 'destroy')->name('auction.delete');
});

Route::controller(BidController::class)->group(function () {
    Route::post('/auction/{id}/bid','placeBid')->name('place.bid');
    Route::get('/profile/{id}/bids/{pageNr}', 'myBids')->name('myBids');
});

Route::view('/blocked', 'pages.blocked');

Route::controller(NotificationController::class)->group(function () {
    Route::get('/profile/{id}/notifications/{pageNr}', 'showNotifications')->name('notifications.user');
    Route::post('/mark-notifications-read', 'markAsRead')->name('mark_notifications_read');
});

Route::controller(ReportAuctionController::class)->group(function (){
    //Route::get('/auctionReport', 'create')->name('report.create');
    Route::post('/auction/{auctionId}/report', 'store')->name('report.store');
    //Route::post('/auctionReport/{id}', 'store')->name('report.store');
    //Route::get('/auction/{id}/reports', 'show')->name('reports.show');
    Route::get('/reports/{pageNr}', 'index')->name('reports.user');
});

Route::controller(ReviewController::class)->group(function (){
    Route::get('/reviews', 'index')->name('reviews.user');
    Route::get('/review/{id}', 'create')->name('reviews.create');
    Route::post('/createReview', 'store')->name('reviews.store');
    Route::get('/all-reviews/{pageNr}', 'allReviews')->name('reviews.admin');
});

Route::controller(TransactionController::class)->group(function (){
    Route::get('/profile/{id}/transactions/{pageNr}', 'index')->name('transactions.user');
});