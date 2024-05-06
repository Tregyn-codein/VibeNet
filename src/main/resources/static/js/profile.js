$(document).ready(function() {
    // Открытие модального окна
    console.log("Скрипт запущен")
    var csrfToken = $("meta[name='_csrf']").attr("content");
    var csrfHeader = $("meta[name='_csrf_header']").attr("content");

    // Закрытие модального окна при клике вне его

    $('#updatePhotoButton').click(function() {
        $('#uploadPhotoModal').show();
    });

    $('#close-profile-photo-modal').click(function() {
        $('#uploadPhotoModal').hide();
    });

    $(document).mouseup( function(e){ // событие клика по веб-документу
        var div = $( "#uploadPhotoModal" ); // тут указываем ID элемента
        if ( !div.is(e.target) // если клик был не по нашему блоку
            && div.has(e.target).length === 0 ) { // и не по его дочерним элементам
            div.hide(); // скрываем его
        }
    });

    $('#edit-username-btn').click(function() {
        $('#editUsernameModal').show();
    });

    // Закрытие модального окна при клике на крестик
    $('#close-username-modal').click(function() {
        $('#editUsernameModal').hide();
    });

    $(document).mouseup( function(e){ // событие клика по веб-документу
        var div = $( "#editUsernameModal" ); // тут указываем ID элемента
        if ( !div.is(e.target) // если клик был не по нашему блоку
            && div.has(e.target).length === 0 ) { // и не по его дочерним элементам
            div.hide(); // скрываем его
        }
    });


    // Обработка формы загрузки
    $('#uploadForm').submit(function(event) {
        event.preventDefault();
        var fileInput = $('#fileInput')[0];
        var file = fileInput.files[0];
        console.log(file.size)
        if (file.size > 524288) { // 512KБ
            alert("Файл слишком большой!");
        } else {
            // Здесь код для отправки файла на сервер
            // Например, используя FormData и AJAX
            if (cropper) {
                cropper.getCroppedCanvas().toBlob(function(blob) {
                    const formData = new FormData();
                    formData.append('croppedImage', blob);
                    cropper.destroy();
                    $.ajax({
                        url: '/uploadPhoto',
                        type: 'POST',
                        data: formData,
                        contentType: false,
                        processData: false,
                        beforeSend: function(xhr) {
                            xhr.setRequestHeader(csrfHeader, csrfToken); // Добавление CSRF токена в заголовок запроса
                        },
                        success: function(response) {
                            $('#uploadPhotoModal').hide();
                            location.reload();
                            // Обновите изображение профиля на странице, если необходимо
                        },
                        error: function() {
                            alert("Ошибка при загрузке файла");
                        }
                    });
                });
            }
        }
    });

    let cropper;
    $('#fileInput').change(function(event) {
        const file = event.target.files[0];
        const reader = new FileReader();
        reader.onload = function(e) {
            $('#uploadForm').css('margin-top','10px');
            $('#imagePreview').attr('src', e.target.result);
            $('#imagePreview').show();
            $('#crop-container').show();
            if (cropper) {
                cropper.destroy();
            }
            cropper = new Cropper($('#imagePreview')[0], {
                aspectRatio: 1, // Обрезка в соотношении сторон 1:1
                viewMode: 1,
                autoCropArea: 1,
                scalable: false,
                zoomable: false,
                guides: false,
                background: false,
                // Другие опции Cropper.js...
            });
        };
        reader.readAsDataURL(file);
    });

    $('#editUsernameForm').submit(function(event) {
        event.preventDefault();
        var usernameInput = $('#usernameInput')[0];
        var username = usernameInput.value;
        console.log(username)
        if (username.length > 15) {
            alert("Имя слишком длинное!");
        } else {
            const formData = new FormData();
            formData.append('username', username);
            $.ajax({
                url: '/updateUsername',
                type: 'POST',
                data: formData,
                contentType: false,
                processData: false,
                beforeSend: function(xhr) {
                    xhr.setRequestHeader(csrfHeader, csrfToken); // Добавление CSRF токена в заголовок запроса
                },
                success: function(response) {
                    $('#editUsernameModal').hide();
                    location.reload();
                    // Обновите изображение профиля на странице, если необходимо
                },
                error: function() {
                    alert("Ошибка при загрузке файла");
                }
            });
        }
    });
});