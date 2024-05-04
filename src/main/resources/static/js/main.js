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

    $('.create-post').submit(function(event) {
        event.preventDefault(); // Предотвращаем стандартную отправку формы

        var formData = {
            content: $('#create-post').val(),
            onlyForFollowers: $('#visionSelect').val() === '2' // Пример, где '2' соответствует "Видно подписчикам"
        };

        var csrfToken = $("meta[name='_csrf']").attr("content");
        var csrfHeader = $("meta[name='_csrf_header']").attr("content");

        $.ajax({
            type: 'POST',
            url: '/create-post', // URL, на который будет отправлен запрос
            contentType: 'application/json',
            data: JSON.stringify(formData),
            dataType: 'json',
            beforeSend: function(xhr) {
                xhr.setRequestHeader(csrfHeader, csrfToken);
            },
            success: function(response) {
                // Обработка успешного ответа
                // Например, очистите форму и обновите список постов
                $('#create-post').val('');
                // Добавьте код для добавления нового поста в DOM
                location.reload();
            },
            error: function(xhr, status, error) {
                // Обработка ошибок при отправке формы
                console.error('Ошибка при отправке формы: ', error);
            }
        });
    });
});