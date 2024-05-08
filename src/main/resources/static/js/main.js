$(document).ready(function() {
    function adjustFeedHeight() {
        var windowHeight = $(window).height();
        var feedHeight = windowHeight - 62 - 40;
        $('#feed').css('height', feedHeight + 'px');
    }

    // Вызовите функцию при первоначальной загрузке страницы
    adjustFeedHeight();

    // Вызовите функцию при изменении размера окна браузера
    $(window).resize(adjustFeedHeight);

    var $createPostBtn = $('#row2');
    var $textarea = $('#create-post');

    $textarea.on('input', function() {
        $(this).css('height', 'auto').css('height', this.scrollHeight + 'px');
    });

    // Сначала скрываем кнопку
    $createPostBtn.hide();

    function isTextareaNotEmpty() {
        return $textarea.val().trim().length > 0;
    }

    // Функция для проверки содержимого textarea и показа/скрытия кнопки
    function toggleButton() {
        if ($textarea.val().trim().length > 0) {
            $('#row').addClass('mb-3');
            $createPostBtn.show();
        } else {
            $('#row').removeClass('mb-3');
            $createPostBtn.hide();
        }
    }

    // Проверяем содержимое textarea при его изменении
    $textarea.on('input', toggleButton);

    // Проверяем содержимое textarea при загрузке страницы
    // на случай, если браузер сохраняет предыдущий ввод
    toggleButton();

    $(window).on('beforeunload', function() {
        if (isTextareaNotEmpty()) {
            return 'На странице есть несохраненные изменения. Вы уверены, что хотите уйти?';
        }
    });

    function trimEmptyLines(text) {
        // Разбиваем текст на массив строк
        let lines = text.split('\n');
        // Удаляем пустые строки в начале
        while (lines.length > 0 && lines[0].trim() === '') {
            lines.shift();
        }
        // Удаляем пустые строки в конце
        while (lines.length > 0 && lines[lines.length - 1].trim() === '') {
            lines.pop();
        }
        // Объединяем массив обратно в строку
        return lines.join('\n');
    }

    var selectedFiles = [];

    $('#post-images').on('change', function(event) {
        // Очищаем предыдущие превью
        $('#image-preview-container').empty();

        // Добавляем новые файлы в массив selectedFiles
        selectedFiles = Array.from(event.target.files);

        // Отображаем превью
        selectedFiles.forEach(function(file, index) {
            var reader = new FileReader();
            reader.onload = function(e) {
                var $slot = $('<div>').addClass('preview-image-slot')
                var $img = $('<img>').attr('src', e.target.result).addClass('preview-image');
                var $removeButton = $('<button>').addClass('remove-image-button').addClass('btn').addClass('btn-danger').html("<span class='material-icons'>delete</span>");
                $removeButton.on('click', function() {
                    // Удаляем изображение из массива
                    selectedFiles.splice(index, 1);
                    // Обновляем превью и input
                    updatePreviewAndInput();
                });
                $('#image-preview-container').append($slot);
                $slot.append($img).append($removeButton);
            };
            reader.readAsDataURL(file);
        });
    });

    function updatePreviewAndInput() {
        // Очищаем контейнер превью
        $('#image-preview-container').empty();

        // Создаем новый input для файлов
        var $newInput = $('<input>').attr({
            type: 'file',
            name: 'images',
            id: 'post-images',
            multiple: '',
            accept: 'image/*'
        });

        // Заменяем старый input новым
        $('#post-images').replaceWith($newInput);

        // Переинициализируем обработчик события change для нового input
        $newInput.on('change', function(event) {
            selectedFiles = Array.from(event.target.files);
            updatePreviewAndInput();
        });

        // Если есть выбранные файлы, создаем DataTransfer объект и добавляем файлы
        if (selectedFiles.length > 0) {
            var dataTransfer = new DataTransfer();
            selectedFiles.forEach(function(file) {
                dataTransfer.items.add(file);
            });
            $newInput[0].files = dataTransfer.files;
        }

        // Отображаем превью для обновленного списка файлов
        selectedFiles.forEach(function(file, index) {
            var reader = new FileReader();
            reader.onload = function(e) {
                var $slot = $('<div>').addClass('preview-image-slot')
                var $img = $('<img>').attr('src', e.target.result).addClass('preview-image');
                var $removeButton = $('<button>').text('Удалить').addClass('remove-image-button').addClass('btn').addClass('btn-danger').html("<span class='material-icons'>delete</span>");
                $removeButton.on('click', function() {
                    selectedFiles.splice(index, 1);
                    updatePreviewAndInput();
                });
                $('#image-preview-container').append($slot);
                $slot.append($img).append($removeButton);
            };
            reader.readAsDataURL(file);
        });
    }

    $('.create-post').submit(function(event) {
        event.preventDefault(); // Предотвращаем стандартную отправку формы

        var formData = new FormData(this);
        formData.append('content', trimEmptyLines($('#create-post').val()));
        formData.append('onlyForFollowers', $('#visionSelect').val() === '2');

        var csrfToken = $("meta[name='_csrf']").attr("content");
        var csrfHeader = $("meta[name='_csrf_header']").attr("content");

        $.ajax({
            type: 'POST',
            url: '/create-post',
            data: formData,
            processData: false,
            contentType: false,
            beforeSend: function(xhr) {
                xhr.setRequestHeader(csrfHeader, csrfToken);
            },
            success: function(response) {
                // Обработка успешного ответа
                // Например, очистите форму и обновите список постов
                $('#create-post').val('');
                $('#image-preview-container').empty();
                // Добавьте код для добавления нового поста в DOM
                location.reload();
            },
            error: function(xhr, status, error) {
                // Обработка ошибок при отправке формы
                alert('Ошибка при отправке формы')
                console.error('Ошибка при отправке формы: ', error);
            }
        });
    });

    $('.posts .post').each(function() {
        var $postContent = $(this).find('.post-content');
        var $showMoreBtn = $(this).find('.show-more');

        var threshold = 2; // Порог в пикселях

        // Проверяем, превышает ли высота содержимого высоту контейнера более чем на пороговое значение
        if ($postContent.prop('scrollHeight') - $postContent.innerHeight() > threshold) {
            $showMoreBtn.show(); // Если текст превышает высоту, показываем кнопку "Показать еще"
        } else {
            $showMoreBtn.hide(); // Иначе скрываем кнопку
        }

        $showMoreBtn.click(function() {
            $postContent.css('max-height', 'none'); // Убираем ограничение по высоте
            $(this).hide(); // Скрываем кнопку
        });
    });

    $('.carousel').carousel();

    function deletePost(postId) {
        var csrfToken = $("meta[name='_csrf']").attr("content");
        var csrfHeader = $("meta[name='_csrf_header']").attr("content");

        $.ajax({
            url: '/delete-post/' + postId, // Предполагается, что у вас есть такой маршрут на сервере
            type: 'DELETE',
            beforeSend: function(xhr) {
                xhr.setRequestHeader(csrfHeader, csrfToken);
            },
            success: function(result) {
                // Удаление поста из DOM или показ сообщения об успехе
                console.log('Пост успешно удален');
                // Например, удалить элемент поста из DOM
                $('div.post[data-post-id="' + postId + '"]').remove();
            },
            error: function(xhr, status, error) {
                // Обработка ошибки
                console.error('Ошибка удаления поста:', error);
            }
        });
    }

    $('.delete-post-btn').on('click', function() {
        var postId = $(this).data('post-id');
        deletePost(postId);
    });
});