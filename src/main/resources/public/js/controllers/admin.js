'use strict';
angular.module('BlogApp')
    .controller('AdminCtrl', function ($scope, $http, $sce) {
        console.log('inside admin controller');
        $scope.html = {};

        $scope.article = {title: '', author: '', htmlContent: '', markdownContent: '',tags: [], preview: ''};
        $scope.preview = function() {
            console.log('From: (Sent) '+$scope.markdownBox);

            $scope.article.markdownContent = $scope.markdownBox;
            $http.post('/api/articles/transform', $scope.markdownBox).success(function (data) {
                console.log('To: (Received) ' + JSON.stringify(data));
                $scope.html.content = $sce.trustAsHtml(data.content);
                $scope.article.htmlContent = data.content;
            });

            $scope.article.title = $scope.title.replace(/\s/g,'-');
            $scope.article.author = $scope.author;
            $scope.article.tags = $scope.tags.split(' ');
            $scope.article.date = new Date();
            $scope.article.year = $scope.article.date.getFullYear();
            console.log($scope.article);

        };

        $scope.submit = function(){
        //Submit article
            $http.post('/api/articles/html/add', $scope.article).success(function(data){
                  //Todo
                alert("Successfully added article!");
            });
        };

        $scope.search = function(){
            console.log('searching by title!!!!!');

            $http.get('/api/articles/title/' +$scope.title.replace(/\s/g,'-') ).success(function (data) {
                console.log('searchByTitle result => ' + JSON.stringify(data));
                $scope.author = data[0].author;
                $scope.markdownBox = data[0].markdownContent;
                var tagz = data[0].tags;
                $scope.tags = '';
                tagz.forEach(function(curr, index, arr){
                    if(index === 0)
                    $scope.tags += curr;
                    else $scope.tags += ' '+curr;
                });

                //$scope.html.content = $sce.trustAsHtml(data.content);
                //$scope.article.content = data.content;
            });
        };

    });