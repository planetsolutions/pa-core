angular.module("app.partials",[]),angular.module("app.partials").run(["$templateCache",function($templateCache){$templateCache.put("frontend/partials/todo/add-todo.html",'<div><h1 translate="pages.add.page.title"></h1><div class="well well-lg"><div static-include="frontend/partials/todo/todo-form.html"></div></div></div>')}]),angular.module("app.partials").run(["$templateCache",function($templateCache){$templateCache.put("frontend/partials/todo/delete-todo-modal.html",'<div class="modal-header"><h4 class="modal-title" translate="dialogs.delete.dialog.title"></h4></div><div class="modal-body"><p>{{"dialogs.delete.dialog.text.prefix" | translate}} {{todo.title}}?</p></div><div class="modal-footer"><button type="button" class="btn btn-default" data-dismiss="modal" translate="dialogs.delete.dialog.cancel.button.label" ng-click="cancel()"></button> <button type="button" class="btn btn-danger" translate="dialogs.delete.dialog.delete.button.label" ng-click="delete()"></button></div>')}]),angular.module("app.partials").run(["$templateCache",function($templateCache){$templateCache.put("frontend/partials/todo/edit-todo.html",'<div><h1 translate="pages.edit.page.title"></h1><div class="well well-lg"><div static-include="frontend/partials/todo/todo-form.html"></div></div></div>')}]),angular.module("app.partials").run(["$templateCache",function($templateCache){$templateCache.put("frontend/partials/todo/todo-form.html",'<form ng-show="todo" name="todoForm" role="form"><div class="form-group"><label for="title" translate="todo.title"></label>: <input id="title" class="form-control" name="title" type="text" placeholder="{{\'todo.title.placeholder\' | translate}}" ng-model="todo.title" required="" ng-maxlength="100"><div class="ng-error" ng-show="todoForm.title.$dirty && todoForm.title.$invalid"><small class="ng-error" translate="todo.messages.title.required" ng-show="todoForm.title.$error.required"></small> <small class="ng-error" translate="todo.messages.title.maxLength" ng-show="todoForm.title.$error.maxlength"></small></div></div><div class="form-group"><label for="type" translate="todo.type"></label>: <input id="type" class="form-control" name="type" type="text" placeholder="{{\'todo.type.placeholder\' | translate}}" ng-model="todo.type" required="" ng-maxlength="100"><div class="ng-error" ng-show="todoForm.type.$dirty && todoForm.type.$invalid"><small class="ng-error" translate="todo.messages.type.required" ng-show="todoForm.type.$error.required"></small> <small class="ng-error" translate="todo.messages.type.maxLength" ng-show="todoForm.type.$error.maxlength"></small></div></div><div class="form-group"><label for="description" translate="todo.description"></label>: <textarea id="description" class="form-control" name="description" placeholder="{{\'todo.description.placeholder\' | translate}}" ng-model="todo.description" ng-maxlength="500">\n        <div class="ng-error" ng-show="todoForm.description.$dirty && todoForm.description.$invalid">\n            <small class="ng-error" translate="todo.messages.description.maxLength" ng-show="todoForm.description.$error.maxlength"></small>\n        </div>\n    </textarea></div><div class="form-group"><label for="data" translate="todo.data"></label>: <textarea id="data" class="form-control" name="data" placeholder="{{\'todo.data.placeholder\' | translate}}" ng-model="todo.data" ng-maxlength="500">\n        <div class="ng-error" ng-show="todoForm.data.$dirty && todoForm.data.$invalid">\n            <small class="ng-error" translate="todo.messages.data.maxLength" ng-show="todoForm.data.$error.maxlength"></small>\n        </div>\n    </textarea></div><link rel="stylesheet" href="/frontend/css/jquery.fileupload.css"><link rel="stylesheet" href="/frontend/css/jquery.fileupload-ui.css"><span class="btn btn-success fileinput-button"><i class="glyphicon glyphicon-plus"></i> <span>Select files...</span><input id="fileupload" type="file" name="files[]" multiple=""></span><br><br><div id="progress" class="progress"><div class="progress-bar progress-bar-success"></div></div><div id="files" class="files"></div><br><div class="form-group"><button type="button" class="btn btn-primary" translate="pages.add.save.todo.button" ng-disabled="todoForm.$invalid || !todoForm.$dirty" ng-click="saveTodo()"></button></div></form><script>\n/*jslint unparam: true, regexp: true */\n/*global window, $ */\n$(function () {\n    \'use strict\';\n    // Change this to the location of your server-side upload handler:\n    var url = \'/api/file/upload\',\n        uploadButton = $(\'<button/>\')\n            .addClass(\'btn btn-primary\')\n            .prop(\'disabled\', true)\n            .text(\'Processing...\')\n            .on(\'click\', function () {\n                var $this = $(this),\n                    data = $this.data();\n                $this\n                    .off(\'click\')\n                    .text(\'Abort\')\n                    .on(\'click\', function () {\n                        $this.remove();\n                        data.abort();\n                    });\n                data.submit().always(function () {\n                    $this.remove();\n                });\n            });\n    $(\'#fileupload\').fileupload({\n        url: url,\n        dataType: \'json\',\n        autoUpload: false,\n        acceptFileTypes: /(\\.|\\/)(gif|jpe?g|png)$/i,\n        maxFileSize: 999000,\n        // Enable image resizing, except for Android and Opera,\n        // which actually support image resizing, but fail to\n        // send Blob objects via XHR requests:\n        disableImageResize: /Android(?!.*Chrome)|Opera/\n            .test(window.navigator.userAgent),\n        previewMaxWidth: 100,\n        previewMaxHeight: 100,\n        previewCrop: true\n    }).on(\'fileuploadadd\', function (e, data) {\n'+"        data.context = $('<div/>').appendTo('#files');\n        $.each(data.files, function (index, file) {\n            var node = $('<p/>')\n                    .append($('<span/>').text(file.name));\n            if (!index) {\n                node\n                    .append('<br>')\n                    .append(uploadButton.clone(true).data(data));\n            }\n            node.appendTo(data.context);\n        });\n    }).on('fileuploadprocessalways', function (e, data) {\n        var index = data.index,\n            file = data.files[index],\n            node = $(data.context.children()[index]);\n        if (file.preview) {\n            node\n                .prepend('<br>')\n                .prepend(file.preview);\n        }\n        if (file.error) {\n            node\n                .append('<br>')\n                .append($('<span class=\"text-danger\"/>').text(file.error));\n        }\n        if (index + 1 === data.files.length) {\n            data.context.find('button')\n                .text('Upload')\n                .prop('disabled', !!data.files.error);\n        }\n    }).on('fileuploadprogressall', function (e, data) {\n        var progress = parseInt(data.loaded / data.total * 100, 10);\n        $('#progress .progress-bar').css(\n            'width',\n            progress + '%'\n        );\n    }).on('fileuploaddone', function (e, data) {\n    	console.log(data);\n        $.each(data.result.files, function (index, file) {\n            if (file.url) {\n                var link = $('<a>')\n                    .attr('target', '_blank')\n                    .prop('href', file.url);\n                $(data.context.children()[index])\n                    .wrap(link);\n            } else if (file.error) {\n                var error = $('<span class=\"text-danger\"/>').text(file.error);\n                $(data.context.children()[index])\n                    .append('<br>')\n                    .append(error);\n            }\n        });\n    }).on('fileuploadfail', function (e, data) {\n        $.each(data.files, function (index) {\n            var error = $('<span class=\"text-danger\"/>').text('File upload failed.');\n            $(data.context.children()[index])\n                .append('<br>')\n                .append(error);\n        });\n    }).prop('disabled', !$.support.fileInput)\n        .parent().addClass($.support.fileInput ? undefined : 'disabled');\n});\n</script>")}]),angular.module("app.partials").run(["$templateCache",function($templateCache){$templateCache.put("frontend/partials/todo/todo-list.html",'<div><h1 translate="pages.list.page.title"></h1><button type="button" class="btn btn-primary" translate="pages.list.page.add.button.label" ng-click="addTodo()"></button><div><div class="well well-sm" dir-paginate="todo in todos | itemsPerPage: pagination.itemsPerPage" current-page="pagination.currentPage" total-items="pagination.totalItems"><a ui-sref="todo.view({todoId: todo.id})">{{todo.title}}</a></div><dir-pagination-controls on-page-change="pageChanged(newPageNumber)" template-url="frontend/partials/directives/dirPagination.tpl.html"></dir-pagination-controls></div><button type="button" class="btn btn-primary" translate="pages.list.page.add.button.label" ng-click="addTodo()"></button></div>')}]),angular.module("app.partials").run(["$templateCache",function($templateCache){$templateCache.put("frontend/partials/todo/view-todo.html",'<div><h1 translate="pages.view.page.title"></h1><div class="well well-lg"><h2>{{todo.title}}</h2><div><p>{{todo.description}}</p></div><div><p><small>{{"todo.creation.time" | translate}}: {{todo.creationTime | amDateFormat:\'DD.MM.YYYY HH:mm:ss\'}} {{"todo.modification.time" | translate }}: {{todo.modificationTime | amDateFormat:\'DD.MM.YYYY HH:mm:ss\'}}</small></p></div><div><button type="button" class="btn btn-primary" translate="pages.view.page.edit.button.label" ng-click="showEditPage()"></button> <button type="button" class="btn btn-danger" translate="pages.view.page.delete.button.label" ng-click="showDeleteDialog()"></button></div></div></div>')}]),angular.module("app.partials").run(["$templateCache",function($templateCache){$templateCache.put("frontend/partials/directives/dirPagination.tpl.html",'<ul class="pagination" ng-if="1 < pages.length"><li ng-if="boundaryLinks" ng-class="{ disabled : pagination.current == 1 }"><a href="" ng-click="setCurrent(1)">&laquo;</a></li><li ng-if="directionLinks" ng-class="{ disabled : pagination.current == 1 }"><a href="" ng-click="setCurrent(pagination.current - 1)">&lsaquo;</a></li><li ng-repeat="pageNumber in pages track by $index" ng-class="{ active : pagination.current == pageNumber, disabled : pageNumber == \'...\' }"><a href="" ng-click="setCurrent(pageNumber)">{{ pageNumber }}</a></li><li ng-if="directionLinks" ng-class="{ disabled : pagination.current == pagination.last }"><a href="" ng-click="setCurrent(pagination.current + 1)">&rsaquo;</a></li><li ng-if="boundaryLinks" ng-class="{ disabled : pagination.current == pagination.last }"><a href="" ng-click="setCurrent(pagination.last)">&raquo;</a></li></ul>')}]),angular.module("app.partials").run(["$templateCache",function($templateCache){$templateCache.put("frontend/partials/search/search-form.html",'<form class="navbar-form navbar-right" name="searchForm" role="search" ng-controller="SearchController"><div class="form-group"><small class="ng-error" ng-show="showMissingCharacterText()">{{missingChars}} {{\'search.missing.characters.text\' | translate}}</small> <input type="text" class="form-control" placeholder="{{\'search.term.field.placeholder\' | translate}}" ng-model="searchTerm" ng-blur="searchFieldBlur()" ng-change="search()" ng-focus="searchFieldFocus()"></div></form>')}]),angular.module("app.partials").run(["$templateCache",function($templateCache){$templateCache.put("frontend/partials/search/search-results.html",'<div><h1 translate="pages.search.results.page.title"></h1><div><div class="well well-sm" dir-paginate="todo in todos | itemsPerPage: pagination.itemsPerPage" current-page="pagination.currentPage" total-items="pagination.totalItems"><a ui-sref="todo.view({todoId: todo.id})">{{todo.title}}</a></div><dir-pagination-controls on-page-change="pageChanged(newPageNumber)" template-url="frontend/partials/directives/dirPagination.tpl.html"></dir-pagination-controls></div></div>')}]);