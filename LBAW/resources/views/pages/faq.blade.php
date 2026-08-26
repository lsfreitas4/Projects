@extends('layouts.app')

@section('title', 'FAQ')

@section('content')

<div class="profile-container"> 
    <h1 class="profile-header">Frequently Asked Questions</h1>
    <div class="user-info" id="about">
    <h3>What do I need to make a Bid? </h3>
    <p>To make a Bid it is necessary to have enough funds for the price indicated, which is slightly higher 
        than the value of the previous Bid. You may not Bid if your Bid is already the highest.
</p>
      <h3>How do I create an Auction? </h3>
      <p>To create an Auction, you may click on the Create Auction option that appears on the top of every page.
</p>
      <h3>Can I close my Auction before the time ends if I'm satisfied with the price?</h3>
      <p>Absolutely! By going to your auction page, click on Close Sooner. You may also change the status of 
        your auction to ending to let the bidders know that it is 
        coming to a close sooner than expected.
</p>
        <h3>I'm trying to delete my Acount and it says I can't because I have an ongoing Auction, what is going on?</h3>
        <p>
        Our policy states that you may only delete your account if you have an auction without bids or if it has bids, 
        then the bids have to be invalid, for example if the users that made them deleted their account. Only then can
        you delete your account. This prevents auctioneers from listing their items for sale and canceling when people
        already started to add funds to Bid.
</p>   
<h3>When can I rate an auctioneer?</h3>
    <p>You can rate an auctioneer everytime you win an auction of theirs. After rating them, you will have to win another 
        one of their auctions to rate them again. We do this to prevent users from manipulating ratings but also to 
        allow them to change their mind if the second auction they won from an auctioneer went differently than the 
        previous ones.
</p>
<h3>After adding funds, can I ever cash them out?</h3>
<p>We want people to spend their money responsibly and so we do not allow users to cash out their account. 
    Instead we ask to you to be careful and think before adding funds and only add as much as you intend 
    to spend on items.
</p>
    </div>
        </div>

@endsection