$(window).on('load', function() {
    // Когда весь контент загружен, скрываем индикатор и показываем содержимое
    $('#loading-indicator').hide();
    $('body').show();
});

$(document).ready(function() {

    if (!localStorage.getItem('background')){
        console.log(!localStorage.getItem('background'));
        localStorage.setItem('background', 'bg-blue');
    }

    $('#loading-indicator').show();
    // Проверяем сохраненный выбор фона и применяем его
    var savedBackground = localStorage.getItem('background');
    if (savedBackground) {
        $('body').addClass(savedBackground);
        if (savedBackground === 'bg-blue') {
            $('header').addClass('grad-blue');
        } else {
            $('header').addClass('grad-pink');
        }
    }

    // Функция для изменения фона
    function changeBackground() {
        var $body = $('body');
        var $header = $('header');
        // Переключаем классы для фона
        if ($body.hasClass('bg-blue')) {
            $body.removeClass('bg-blue').addClass('bg-pink');
            $header.removeClass('grad-blue').addClass('grad-pink');
            localStorage.setItem('background', 'bg-pink');
        } else {
            $body.removeClass('bg-pink').addClass('bg-blue');
            $header.removeClass('grad-pink').addClass('grad-blue');
            localStorage.setItem('background', 'bg-blue');
        }
    }


    // Обработчик клика по кнопке
    $('#change-background').click(changeBackground);
    $(window).trigger('load');
});