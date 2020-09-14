/**

 * Created by Behrouz on 8/22/16.
 */
//var app = angular.module('plnApplication', ['ngRoute','ngTagsInput','plnModule']);
var app = angular.module('plnApplication', ['ngRoute','ngTagsInput','ngTable', 'plnModule', 'ui.bootstrap']);
//var app = angular.module('plnApplication', ['plnModule','ngRoute']);

app.config(['$routeProvider', '$locationProvider', function($routeProvider, $locationProvider) {
    $routeProvider.
    //when('/network-view', {templateUrl: 'partials/assay-view.html',   controller: 'MainCtrl' }).
    // when('/', {templateUrl: 'partials/enrichment.html',
    //     controller: 'MainCtrl',
    //     controllerAs: 'ctrl' }).
    when('/error', {templateUrl: 'partials/500.html',
        controller: 'MainCtrl',
        controllerAs: 'ctrl' }).
    when('/enrichment', {templateUrl: 'partials/enrichment.html',
        controller: 'MainCtrl',
        controllerAs: 'ctrl' }).
    when('/peptideToProtein', {templateUrl: 'partials/peptideToProtein.html',
        controller: 'MainCtrl',
        controllerAs: 'ctrl' }).
    when('/peptideToProtein/:dataBase/:proteinForm/:list/:organism/:format', {templateUrl: 'partials/peptideToProtein.html',
        controller: 'MainCtrl',
        controllerAs: 'ctrl' }).
    when('/ptmToModifier/:list/:organism', {templateUrl: 'partials/ptmToModifier.html',
        controller: 'ProteinCtrl',
        controllerAs: 'ctrl' }).
    when('/ptmToModifier', {templateUrl: 'partials/ptmToModifier.html',
        controller: 'ProteinCtrl',
        controllerAs: 'ctrl' }).
    // when('/proteinToPathway/:id', {templateUrl: 'partials/ptmToModifier.html',
    //     controller: 'MainCtrl',
    //     controllerAs: 'ctrl' }).
    when('/proteinToPathway/:list/:organism', {templateUrl: 'partials/proteinToPathway.html',
        controller: 'PathwayCtrl',
        controllerAs: 'ctrl' }).
    when('/proteinToPathway', {templateUrl: 'partials/proteinToPathway.html',
        controller: 'PathwayCtrl',
        controllerAs: 'ctrl' }).
    when('/ptm', {templateUrl: 'partials/ptm.html',
        controller: 'MainCtrl',
        controllerAs: 'ctrl' }).
    when('/contact', {templateUrl: 'partials/contact.html',
        controller: 'MainCtrl',
        controllerAs: 'ctrl'}).
    when('/modification', {templateUrl: 'partials/modification.html',
        controller: 'ModificationCtrl',
        controllerAs: 'ctrl'}).
    when('/uploadStatus', {templateUrl: 'partials/uploadStatus.html',
            controller: 'MainCtrl',
            controllerAs: 'ctrl'}).
    when('/help', {templateUrl: 'partials/help.html',
        controller: 'AboutCtrl',
        controllerAs: 'ctrl'}).
    when('/kegg', {templateUrl: 'partials/kegg.html',
        controller: 'KEGGCtrl',
        controllerAs: 'ctrl'}).
    when('/splash', {templateUrl: '/',
        controller: 'MainCtrl',
        controllerAs: 'ctrl'}).
    when('/upload', {templateUrl: 'partials/upload.html',
            controller: 'MainCtrl',
            controllerAs: 'ctrl'}).
    // when('/error', {
    //         controller: ['$location', function($location){
    //             $location.replace('/');
    //         }]
    //     }).
    // when('/pinet', {templateUrl: 'partials/peptideToProtein.html',
    //     controller: 'MainCtrl',
    //     controllerAs: 'ctrl' }).
    otherwise({
        redirectTo: '/peptideToProtein'});
    // otherwise({
    //     redirectTo: '/'
    // });
    // .otherwise({
    //     redirectTo: function() {
    //         window.location = "/";
    //     }
    // });
    // use the HTML5 History API
    $locationProvider.html5Mode(true);

}]);
//
//
// app.filter('treeJSON', function () {
//     function prettyPrintJson(json) {
//         return JSON ? '\n' + JSON.stringify(json, null, ' ') : 'your browser does not support JSON so can not show the Json format.';
//     }
//
//     return prettyPrintJson;
// });
//
// app.filter('inline', function () {
//     function inlinePLN(plnArray) {
//         var output = '\n';
//         var layerSep;
//         var groupSep;
//         var itemSep;
//         var FormatREF;
//         var FormatSYM;
//         var FormatDES;
//         var FormatVAR;
//         var FormatPTM;
//         for (var i = 0; i < plnArray.length; i++) {
//             var plnLocal = plnArray[i];
//
//             var plnKey = Object.keys(plnLocal.PLN)[0];
//             var plnValue = plnLocal.PLN[plnKey];
//
//             var refKey = Object.keys(plnLocal.REF)[0];
//             var refValue = plnLocal.REF[refKey];
//
//             var symKey = Object.keys(plnLocal.SYM)[0];
//             var symValue = plnLocal.SYM[symKey];
//
//             var PTM = [];
//             itemSep = "&";
//             if (plnValue == "InChl-like") {
//                 layerSep = "/";
//                 groupSep = ";";
//                 FormatREF = "r=";
//                 FormatSYM = "s=";
//                 FormatDES = "d=";
//                 FormatVAR = "v=";
//                 FormatPTM = "m=";
//             } else {
//                 layerSep = ";";
//                 groupSep = ",";
//                 FormatREF = "REF=";
//                 FormatSYM = "SYM=";
//                 FormatDES = "DES=";
//                 FormatVAR = "VAR=";
//                 FormatPTM = "PTM=";
//
//             }
//
//
//             plnLocal.PTM.map(function (ptmGroup) {
//                     //console.log(ptmGroup);
//
//                     var ptmForHit = [];
//
//                     for (var index = 0; index < ptmGroup.length; index++) {
//                         ptmForHit.push(ptmGroup[index].identifier + "@" + ptmGroup[index].location);
//                     }
//
//                     PTM.push(ptmForHit.join(itemSep));
//                 }
//             );
//
//             output = output +
//                 "PLN=" + plnKey + ":" + plnValue + layerSep +
//                 FormatREF + refKey + ":" + refValue.join(groupSep + refKey + ":") + layerSep +
//                 FormatSYM + symKey + ":" + symValue.join(groupSep + symKey + ":") + layerSep +
//                 FormatDES + layerSep +
//                 FormatVAR + layerSep +
//                 FormatPTM + PTM.join(groupSep) + "#" + "\n\n";
//
//
//         }
//         return output;
//     }
//
//     return inlinePLN;
// });