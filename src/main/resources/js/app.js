var app = angular.module('Blogapp', []);
//
//angular.module('Blogapp', ['ngRoute'])
//    .config(function ($routeProvider, $locationProvider) {
//        $locationProvider.html5Mode(true);
//        $routeProvider
//            .when('/', {
//                templateUrl: '/index.html',
//                controller: 'MainCtrl'
//            }).when('/articles', {
//                templateUrl: '/partials/main.html',
//                controller: 'MainCtrl'
//            })
//            .when('/articles/new', {
//                templateUrl: '/partials/new_article.html'
//            })
//            .when('/articles/:id', {
//                templateUrl: '/partials/article.ftl',
//                controller: 'ArticleCtrl'
//            })
//            .when('/login', {
//                templateUrl: '/partials/login.html',
//                controller: 'LoginCtrl'
//            })
//            .otherwise({
//                redirectTo: '/'
//            });
//
//
//    });