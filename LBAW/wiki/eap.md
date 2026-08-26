# EAP: Architecture Specification and Prototype

> For people that like to buy or sell designer clothes through auctions and enjoy incredible deals, StyleSwap gives you an amazing experience while lets you browse through a vast array of items up for auction as well as create your own so that you may find that piece of clothing that was missing in your wardrobe or sell the one that was taking up the space for a generous amount.


## A7: Web Resources Specification

> This artifact presents the documentation for StyleSwap, including the CRUD (create, read, update, delete) operations available for each resource.

### 1. Overview

> Identify and overview the modules that will be part of the application.  

<table>
    <tr>
        <td><strong>M01: Authentication and User Profile</strong></td>
        <td>Web Resources associated with authenticating into the system and managing each user's profile. Includes the following system features: login/logout, registration, password recovery, viewing and editing personal information, see your own auctions, add credit, see transaction history.</td>
    </tr>
    <tr>
        <td><strong>M02: User Administration/Static Pages</strong></td>
        <td>Web resources associated with user administration and platform moderation. Includes the following system features: view reports, block/unblock users, delete auctions, delete user accounts, view and change user information, and view system access details for each user. Web resources including static content that are associated with this module: FAQ, About Us, Contacts, Services.</td>   
    </tr>
    <tr>
        <td><strong>M03: Bids</strong></td>
        <td>Web-based resources for bidding that come with user-friendly features, including the ability to place bids and receive notifications about those bids. These notifications will let you know when your bids are outbid or when they secure the highest bid at the end of the auction.</td>
    </tr>
    <tr>
        <td><strong>M04: Auctions</strong></td>
        <td>Web-based resources for auctions, offering a range of useful features such as creating, canceling, or closing auctions, editing auction details, receiving auction notifications, managing auction status, searching for auctions with filters, and reporting auctions when needed.</td>
    </tr>
    <tr>
        <td><strong>M05: Review and Follow</strong></td>
        <td>Explore web resources for following and reviewing, with user-friendly features like the ability to follow or unfollow auctions, receive notifications, give or delete reviews, and view the auctioneer's rating.</td>
    </tr>   
</table>

### 2. Permissions

> Define the permissions used by each module, necessary to access its data and features.  

<table>
    <tr>
        <td><strong>Permission</strong></td>
        <td><strong>Name</strong></td>
        <td><strong>Description</strong></td>
    </tr>
    <tr>
        <td><strong>PUB</strong></td>
        <td>Public</td>
        <td>Visitors without privileges and can only browse through auctions.</td>
    </tr>
    <tr>
        <td><strong>USR</strong></td>
        <td>User</td>
        <td>Authenticated users, who can make bids and become auctioneer in order to create their own auctions.</td>
    </tr>
    <tr>
        <td><strong>ACT</strong></td>
        <td>Auctioneer</td>
        <td>Special user that can create auctions (the user can still bid on other auctions).</td>
    </tr>
    <tr>
        <td><strong>OWN</strong></td>
        <td>Owner</td>
        <td>User that are owners of the information (own profile, own auctions (auctioneers).</td>
    </tr>
    <tr>
        <td><strong>ADM</strong></td>
        <td>Administrator</td>
        <td>System administrators that manage the whole website.</td>
    </tr>
</table>

### 3. OpenAPI Specification

> OpenAPI specification in YAML format to describe the vertical prototype's web resources.

> Link to the [`a7_openapi.yaml`](https://git.fe.up.pt/lbaw/lbaw2324/lbaw23152/-/blob/main/a7_openapi.yaml?ref_type=heads) file in the group's repository.


```yaml
openapi: 3.0.0

info:
  version: "1.0"
  title: "LBAW StyleSwap Web API"
  description: "Web Resources Specification (A7) for StyleSwap"

servers: 
- url: https://lbaw23152.lbaw.fe.up.pt
  description: Production Server 

externalDocs:
 description: Find more info here.
 url: https://git.fe.up.pt/lbaw/lbaw2324/lbaw23152/-/wikis/home

tags:
  - name: 'M01: Authentication and User Profile'
  - name: 'M02: User Administration/ Static Pages'
  - name: 'M03: Bids'
  - name: 'M04: Auctions'
  - name: 'M05: Review and Follow'


paths:

  /login:
    get:
      operationId: R101
      summary: 'R101: Login Form'
      description: 'Provide login form. Access: PUB'
      tags:
        - 'M01: Authentication and User Profile'

      responses:
        '200':
          description: 'Ok. Show login UI'


    post:
      operationId: R102
      summary: 'R102: Login action'
      description: 'Process the login form data. Access: PUB'
      tags: 
        - 'M01: Authentication and User Profile'

      requestBody:
        required: true
        content: 
          application/x-www-form-urlencoded:
            schema:
              type: object
              properties:
                email:
                  type: string
                password: 
                  type: string
                remember:
                  type: boolean
                required:
                  - email
                  - password
                  - remember

      responses:
        '302':
          description: 'Found. Redirect after processing the login credentials.'
          headers:
            Location: 
              schema:   
                type: string
              examples:
                302Success: 
                  description: 'Successful authentication: Redirecting to home page.'
                  value: '/home'
                302Error:
                  description: 'Failed to authenticate user, going back to login page.'
                  value: '/login'

  /logout:
    get: 
      operationId: R103
      summary: 'R103: Logout Action'
      description: 'Logout the current authenticated user. Access: USR, ADM'
      tags: 
        - 'M01: Authentication and User Profile'
      responses:
        '302':
          description: 'Redirect after processing logout.'
          headers: 
            Location:   
              schema:  
                type: string
              examples:
                302Success: 
                  description: 'Successful logout. Redirect to login form.'
                  value: '/login'

  /register:
    get:
      operationId: R104
      summary: 'R104: Register Form'
      description: 'Provide new user registration form. Access: PUB'
      tags: 
        - 'M01: Authentication and User Profile'
      responses:
        '200':
          description: 'Ok. Show sign-up UI'
    
    post:
      operationId: R105
      summary: 'R105: Register Action'
      description: 'Processes the new user registration form submission. Access: PUB'
      tags: 
        - 'M01: Authentication and User Profile'

      requestBody:
        required: true
        content:
          application/x-www-form-urlencoded:
            schema:
              type: object
              properties:
                username:
                  type: string
                first_name:
                  type: string
                last_name:
                  type: string
                email: 
                  type: string
                password:
                  type: string
              required:
                - username
                - first_name
                - last_name
                - email
                - password

      responses:
        '302':
          description: 'Redirect after processing the new user information.'
          headers:
            Location:
              schema:
                type: string
              examples:
                302Success:
                  description: 'Successful registration. Redirect to main page.'
                  value: '/home'
                302Failure: 
                  description: 'Failed registration, redirect to register form.'
                  value: '/register' 

  /profile/{id}:
    get:
      operationId: R106
      summary: 'R106: View user profile'
      description: 'Show the user profile. Access: USR, OWN, ADM'
      tags: 
        - 'M01: Authentication and User Profile'
      parameters:
        - in: path
          name: id
          schema: 
            type: integer
          required: true

      responses:
        '200':
          description: 'Ok. Show profile UI'
        '403':
          description: 'Unauthorized'  

  /profile/{id}/edit:
    get:
      operationId: R107
      summary: 'R107: View edit user profile form'
      description: 'Show the user profile. Access: OWN, ADM'
      tags: 
        - 'M01: Authentication and User Profile'
      parameters:
        - in: path
          name: id
          schema: 
            type: integer
          required: true

      responses:
        '200':
          description: 'Ok. Show edit profile form'
        '403':
          description: 'Unauthorized'

    post:
      operationId: R108
      summary: 'R108: Edit user profile action'
      description: 'Processes the edit of a user profile. Access: OWN, ADM'
      tags: 
        - 'M01: Authentication and User Profile'

      requestBody:
        required: true
        content:
          application/x-www-form-urlencoded:
            schema:
              type: object
              properties:
                first_name:
                  type: string
                last_name:
                  type: string
                password:
                  type: string
                address:
                  type: string
              required:
                - first_name
                - last_name
                - password
                - address

      responses:
        '302':
          description: 'Redirect after processing the user profile update.'
          headers:
            Location:
              schema:
                type: string
              examples:
                302Success:
                  description: 'Successful profile update. Redirecting to user profile page.'
                  value: '/profile/{id}'
                302Failure:
                  description: 'Failed to update profile, redirecting to edit form.'
                  value: '/profile/{id}/edit'
        '403':
          description: 'Forbidden. User not authorized to edit this profile.'
        '404':
          description: 'Not Found. No profile found with the given ID.'


  /profile/{id}/balance:
    get:
      operationId: R109
      summary: 'R109: Add funds form'
      description: 'Add funds to the user. Access: OWN'
      tags: 
        - 'M01: Authentication and User Profile'
      parameters:
        - in: path
          name: id
          schema:
            type: integer
          required: true

      responses:
        '200': 
          description: 'Ok. Show add funds form'

    post:
      operationId: R110
      summary: 'R110: Add funds action'
      description: 'Processes the addition of funds to a user. Access: OWN'
      tags: 
        - 'M01: Authentication and User Profile'
      parameters:
        - in: path
          name: id
          schema:
            type: integer
          required: true
      requestBody:
        required: true
        content:
          application/x-www-form-urlencoded:
            schema:
              type: object
              properties:
                funds:
                  type: number
              required:
                - funds

      responses:
        '302':
          description: 'Redirect after processing the addition of funds.'
          headers:
            Location:
              schema:
                type: string
              examples:
                302Success:
                  description: 'Successful addition of funds. Redirecting to user profile page.'
                  value: '/profile/{id}'
                302Failure: 
                  description: 'Failed to add funds, redirecting to add funds form.'
                  value: '/profile/{id}/balance'
        '400':
          description: 'Bad request. Incorrect or insufficient data provided.'
        '403':
          description: 'Forbidden. User not authorized to add funds to this profile.'
        '404':
          description: 'Not Found. No user found with the given ID.'

  /profile/{id}/auctions/{pageNr}:
    get:
      operationId: R111
      summary: 'R111: User´s auctions list'
      description: 'Show the list of an user´s auctions. Access: USR, ADM'
      tags: 
        - 'M01: Authentication and User Profile'
      parameters:
        - in: path
          name: id
          schema: 
            type: integer
          required: true
        - in: path
          name: pageNr
          schema:
            type: integer
          required: true

      responses:
        '200':
          description: 'Ok. Show user´s auctions list'            

  /profile/{id}/bids/{pageNr}:
    get:
      operationId: R112
      summary: 'R112: User´s bids list'
      description: 'Show the list of an user´s bids. Access: OWN, ADM'
      tags: 
        - 'M01: Authentication and User Profile'
      parameters:
        - in: path
          name: id
          schema: 
            type: integer
          required: true
        - in: path
          name: pageNr
          schema:
            type: integer
          required: true

      responses:
        '200':
          description: 'Ok. Show user´s bids list'

  /users/{pageNr}:
    get:
      operationId: R113
      summary: 'R113: Users list by page'
      description: 'Show the list of users by page. Access: USR, ADM'
      tags: 
        - 'M01: Authentication and User Profile'
      parameters:
        - in: path
          name: pageNr
          schema: 
            type: integer
          required: true
      
      responses:
        '200':
          description: 'Ok. Show users list by page.'

  /search:
    get: 
      operationId: R114
      summary: 'R114: Main search page'
      description: 'Show the main search page for various entities (users, auctions, etc.). Access: USR, ADM'
      tags: 
        - 'M01: Authentication and User Profile'
      parameters:
        - in: query
          name: q
          schema:
            type: string
          required: false

      responses:
        '200':
          description: 'Ok. Show search results.'

  /users/{pageNr}/search:
    get:
      operationId: R115
      summary: 'R115: Search User with Exact Match'
      description: 'Perform an exact match search for users. Access: USR, ADM'
      tags:
        - 'M01: Authentication and User Profile'
      parameters:
        - in: path
          name: pageNr
          schema:
            type: integer
          required: true
        - in: query
          name: query
          schema:
            type: string
          required: true
      responses:
        '200':
          description: 'Ok. Show exact match search results for users'

  /profile/{id}/promote:
    post:
      operationId: R201
      summary: 'R201: Promote User to Admin'
      description: 'Promotes a specified user to ADMIN. Access: ADM'
      tags: 
        - 'M02: User Administration/ Static Pages'
      parameters:
        - in: path
          name: id
          schema: 
            type: integer
          required: true
      responses:
        '302':
          description: 'Redirect after attempting to promote the user.'
          headers:
            Location:
              schema:
                type: string
              examples:
                302Success:
                  description: 'Successful promotion. Redirecting to user profile page.'
                  value: '/profile/{id}'
                302Failure:
                  description: 'Failed to promote user, redirecting to user profile page.'
                  value: '/profile/{id}'
        '403':
          description: 'Forbidden. Only admins are authorized to promote users.'
        '404':
          description: 'Not Found. No user found with the given ID.'

  /auction/{id}/bid:
    post: 
      operationId: R301
      summary: 'R301: Make bid'
      description: 'Makes a bid on a specified auction and returns the result as JSON. Access: USR.'
      tags:
        - 'M03: Bids'
      parameters:
        - in: path
          name: auctionId
          schema:
            type: integer
          required: true
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              properties:
                bid:
                  type: number
                  description: 'Value of the bid'

      responses:
        '200':
          description: 'Success'
          content:
            application/json:
              schema:
                type: object
                properties:
                  id:
                    type: integer
                  auction:
                    type: integer
                  user:
                    type: integer
                  value:
                    type: number
              example:
                id: 1
                auction: 6
                user: 2
                value: 5000

  /auction/{id}:
    get:
      operationId: R401
      summary: 'R401: View individual auction'
      description: 'Show the individual page of an auction. Access: PUB'
      tags:
        - 'M04: Auctions'
      parameters:
        - in: path
          name: auctionId
          schema:
            type: integer
          required: true

      responses:
        '200':
          description: 'Ok. Show the auction page'

  /auctions/{pageNr}:
      get:
        operationId: R402
        summary: 'R402: All auctions list'
        description: 'List of All Auctions by Page (sorted by auctions with the shortest time until closing first, 5 per page): Access PUB'
        tags:
          - 'M04: Auctions'
        parameters:
          - in: path
            name: pageNr
            schema:
              type: integer
            required: true

        responses:
          '200':
            description: 'Ok. Show users auctions list by page' 

  /auctionCreate:
    get:
      operationId: R403
      summary: 'R403: Create Auction Form'
      description: 'Show the form to create an auction. Access: ACT'
      tags:
        - 'M04: Auctions'
      responses:
        '200':
          description: 'Ok. Show auction create form'

    post:
      operationId: R404
      summary: 'R404: Create Auction'
      description: 'Processes the creation of an auction. Access: ACT'
      tags:
        - 'M04: Auctions'
      requestBody:
        required: true
        content:
          application/x-www-form-urlencoded:
            schema:
              type: object
              properties:
                title:
                  type: string
                description:
                  type: string
                name:
                  type: string
                category:
                  type: string
                brand:
                  type: string
                color:
                  type: string
                condition:
                  type: string
                picture:
                  type: string
                starting_price:
                  type: string
                end_date:
                  type: string
                status:
                  type: string
                user:
                  type: string
              required:
                - title
                - description
                - name
                - category
                - status
                - color
                - condition
                - picture
                - starting_price
                - end_date
                - user

      responses:
        '302':
          description: 'Forwarding after completing the auction creation process.'
          headers:
            Location:
              schema:
                type: string
              examples:
                302Success:
                  description: 'Successful creation. Redirect to created auction page.'
                  value: '/auction/{id}'
                302Failure:
                  description: 'Creation failed. Redirect to home.'
                  value: '/home'

  /auction/{id}/edit:
    get:
      operationId: R405
      summary: 'R405: Edit an Auction'
      description: 'Show the form to edit an auction. Access: ACT/OWN'
      tags:
        - 'M04: Auctions'
      parameters:
        - in: path
          name: auctionId
          schema:
            type: integer
          required: true
      responses:
        '200':
          description: 'Success. Show edit form of the auction.'

    post:
      operationId: R406
      summary: 'R406: Action of Edit an Auction'
      description: 'Processes the edit of an auction. Access: ACT/OWN'
      tags:
        - 'M04: Auctions'
      parameters:
        - in: path
          name: id
          schema:
            type: integer
          required: true
      requestBody:
        required: true
        content:
          application/x-www-form-urlencoded:
            schema:
              type: object
              properties:
                title:
                  type: string
                description:
                  type: string
              required:
                - title
                - description
      responses:
        '302':
          description: 'Forwarding upon completing the modification of an auction.'
          headers:
            Location:
              schema:
                type: string
              examples:
                302Success:
                  description: 'Success. Forwarding to edited auction page.'
                  value: '/auction/{id}'
                302Failure:
                  description: 'Fail. Return to edit auction page.'
                  value: '/auction/{id}/edit'

  /auction/{id}/cancel:
    put:
      operationId: R407
      summary: 'R407: Cancel Auction'
      description: 'Canceling process of an auction. Access: ACT/OWN'
      tags:
        - 'M04: Auctions'
      parameters:
        - in: path
          name: auctionId
          schema:
            type: integer
          required: true
      responses:
        '302':
          description: 'Forwarding after processing the cancellation of an auction.'
          headers:
            Location:
              schema:
                type: string
              examples:
                302Success:
                  description: 'Success on the cancellation. Forwarding to auction.'
                  value: '/auction/{id}/'
                302Failure:
                  description: 'Failed to cancel the auction. Forwarding to auction.'
                  value: '/auction/{id}/'

  /auctions/{pageNr}/search:
    get:
      operationId: R408
      summary: 'R408: Search Auction with Full Text Search'
      description: 'Perform a full-text search for auctions. Access: USR, ADM'
      tags:
        - 'M04: Auctions'
      parameters:
        - in: path
          name: pageNr
          schema:
            type: integer
          required: true
        - in: query
          name: query
          schema:
            type: string
          required: true
      responses:
        '200':
          description: 'Ok. Show full-text search results for auctions'


```

---


## A8: Vertical prototype

> This sections describes the features implemented for the prototype of the product, including a list of the user stories that are addressed and the web resources developed.

### 1. Implemented Features

#### 1.1. Implemented User Stories

> Identify the user stories that were implemented in the prototype.  

| User Story reference | Name                   | Priority                   | Description                   |
| -------------------- | ---------------------- | -------------------------- | ----------------------------- |
| US101 | See Home | high | As a User, I want to be able to access the home page, so that I can see a brief presentation of the website. |
| US201 | Visitor Login | high | As a Visitor, I want to authenticate into the system, so that I can access privileged information. |
| US202 | Register | high | As a Visitor, I want to register myself into the system, so that I can authenticate myself into the system. |
| US203 | View Auction Page (Visitor) | high | As a Visitor, I want to be able to see an auction page, so that I can have access to the information about it. |
| US204 | View Other People’s Profile | high | As a Visitor, I want to be able to see other people's profiles, so that I can learn more information about them. |
| US301 | Authenticated User Logout | high | As an Authenticated User, I want to logout from the app, so that I can let another user access the app in my system without compromising my private data. |
| US302 | View profile | high | As an Authenticated User, I want to view my user profile, so that I can be aware of the information the system has related to my account (both public and private information). . |
| US303 | Edit Profile | high | As an Authenticated User, I want to edit my user profile, so that I can update the information the system has related to my account (both public and private information). |
| US304 | Create Auction | high | As an Authenticated User, I want to be able to create an auction, so I can sell my items. |
| US305 | Bid on Auction | high | As an Authenticated User, I want to be able to place my bids in an item's auction, so that I can try to buy it. |
| US306 | View Auction Page (Authenticated User | high | As an Authenticated User, I want to be able to see an auction page, so that I can have access to the information about it. |
| US309 | View my Bidding History | medium | As an Authenticated User, I want to be able to see my bid history, so that I can see in which auctions I’m in. |
| US310 | General Search | medium | As an Authenticated User, I want to be able to search all the information made available by the system, such as users and auctions. |
| US314 | Add Credit to Account | medium | As an Authenticated User, I want to be able to add credit to my account, so that I can use that to bid on auctions. |
| US315 | View Placeholders | medium | As an Authenticated User, I want to see placeholders in form inputs, so that I know what information is required. |
| US401 | View Auction Bidding History | high | As a Bidder, I want to be able to view the auction bidding history, so that I can decide if I want to make a bid on that auction. |
| US501 | Edit Auction | high | As an Auctioneer, I want to be able to edit my auction, so that I can correct any mistakes. |
| US502 | Cancel Auction | high | As an Auctioneer, I want to be able to cancel my auction so that I can stop it if I change my mind and don’t want to sell my item anymore. |
| US503 | View Own Auctions | high | As an Auctioneer, I want to be able to see all my auctions so that I can quickly check up on their status. |
| US602 | Administer User Accounts | high | As an Administrator, I want to be able to administer user accounts, so that I can search, view, edit or create accounts. |
| US607 | Administrator Login | high | As an Administrator, I want to securely log in, so that I can perform administrative duties such as managing user accounts and auctions. |
| US608 | Administrator Logout | high | As an Administrator, I want to securely log out, so that I can securely leave without compromising the privacy of the whole system. |



#### 1.2. Implemented Web Resources

> Identify the web resources that were implemented in the prototype.  

> Module M01: Authentication and User Profile  

| Web Resource Reference | URL                            |
| ---------------------- | ------------------------------ |
| R101: Login Form | GET [/login](https://lbaw23152.lbaw.fe.up.pt/login) |
| R102: Login action | POST /login |
| R103: Logout Action | POST /logout |
| R104: Register Form | GET [/register](https://lbaw23152.lbaw.fe.up.pt/register) |
| R105: Register Action | POST /register |
| R106: View user profile | GET [/profile/{id}](https://lbaw23152.lbaw.fe.up.pt/profile/1) | |
| R107: View edit user profile form | GET [/profile/{id}/edit](https://lbaw23152.lbaw.fe.up.pt/profile/1/edit) |
| R108: Edit user profile action | POST /profile/{id}/edit |
| R109: Add funds form |GET [/profile/{id}/balance](https://lbaw23152.lbaw.fe.up.pt/profile/1/balance) |
| R110: Add funds action | POST /profile/{id}/balance|
| R111: Users auctions list | GET [/profile/{id}/auctions/{pageNr}](https://lbaw23152.lbaw.fe.up.pt/profile/1/auctions/1) |
| R112: Users bids list | GET [/profile/{id}/bids/{pageNr}](https://lbaw23152.lbaw.fe.up.pt/profile/1/bids/1) |
| R113: Users list by page | GET [/users/{id}](https://lbaw23152.lbaw.fe.up.pt/users/1)|
| R114: Main search page | GET [/home](https://lbaw23152.lbaw.fe.up.pt/home) |
| R115: Search User with Exact Match | GET [/users/{id}](https://lbaw23152.lbaw.fe.up.pt/users/1?query=) |

> Module M02: User Administration/Static Pages

| Web Resource Reference | URL                            |
| ---------------------- | ------------------------------ |
| R201: Promote User to Admin | POST /profile/{id}/promote|

> Module M03: Bids  

| Web Resource Reference | URL                            |
| ---------------------- | ------------------------------ |
| R301: Make bid | POST /auction/{id}/bid |


> Module M04: Auctions  

| Web Resource Reference | URL                            |
| ---------------------- | ------------------------------ |
| R401: View individual auction | GET [/auction/{id}](https://lbaw23152.lbaw.fe.up.pt/auction/1) |
| R402: All auctions list | GET [/auctions/{pageNr}](https://lbaw23152.lbaw.fe.up.pt/auctions/1) |
| R403: Create Auction Form | GET [/auctionCreate](https://lbaw23152.lbaw.fe.up.pt/auctionCreate) |
| R404: Create Auction | POST /auctionCreate |
| R405: Edit an Auction | GET [/auction/{id}/edit](https://lbaw23152.lbaw.fe.up.pt/auction/1/edit)|
| R406: Action of Edit an Auction | POST /auction/{id}/edit |
| R407: Cancel Auction | POST /auction/{id}|
| R408: Search Auction with Full Text Search | GET [/auctions/{id}](https://lbaw23152.lbaw.fe.up.pt/auctions/1?query=) |

### 2. Prototype

The prototype can be found at https://lbaw23152.lbaw.fe.up.pt

Credentials:
* Admin user: sarahm@example.com / password: password
* User: lindaw@example.com / password: password

The code is available at: https://git.fe.up.pt/lbaw/lbaw2324/lbaw23152


---


## Revision history

Changes made to the first submission:
1.

***

GROUP23152, 23/11/2023
 
* Beatriz Cruz, up201905517@up.pt 
* Manuel Alves, up201906910@up.pt (editor)
* João Moura, up201904881@up.pt
* Luís Freitas, up201905767@up.pt