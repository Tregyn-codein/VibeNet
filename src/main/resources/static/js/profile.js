$(document).ready(function() {
    // Открытие модального окна
    console.log("Скрипт запущен")
    var csrfToken = $("meta[name='_csrf']").attr("content");
    var csrfHeader = $("meta[name='_csrf_header']").attr("content");

    $('#updatePhotoButton').click(function() {
        $('#myModal').show();
    });

    // Закрытие модального окна при клике на крестик
    $('.close').click(function() {
        $('#myModal').hide();
    });

    // Закрытие модального окна при клике вне его
    $(window).click(function(event) {
        if ($(event.target).is('#myModal')) {
            $('#myModal').hide();
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
                            $('#myModal').hide();
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
            $('#container').show();
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


    // Список c превью
    // var dt = new DataTransfer();
    //
    // $('.input-file input[type=file]').on('change', function(){
    //     let $files_list = $(this).closest('.input-file').next();
    //     $files_list.empty();
    //
    //     for(var i = 0; i < this.files.length; i++){
    //         let file = this.files.item(i);
    //         dt.items.add(file);
    //
    //         let reader = new FileReader();
    //         reader.readAsDataURL(file);
    //         reader.onloadend = function(){
    //             let new_file_input = '<div class="input-file-list-item">' +
    //                 '<img class="input-file-list-img" src="' + reader.result + '">' +
    //                 '<span class="input-file-list-name">' + file.name + '</span>' +
    //                 '<a href="#" onclick="removeFilesItem(this); return false;" class="input-file-list-remove">&times;</a>' +
    //                 '</div>';
    //             $files_list.append(new_file_input);
    //         }
    //     };
    //     this.files = dt.files;
    // });
    //
    // function removeFilesItem(target){
    //     let name = $(target).prev().text();
    //     let input = $(target).closest('.input-file-row').find('input[type=file]');
    //     $(target).closest('.input-file-list-item').remove();
    //     for(let i = 0; i < dt.items.length; i++){
    //         if(name === dt.items[i].getAsFile().name){
    //             dt.items.remove(i);
    //         }
    //     }
    //     input[0].files = dt.files;
    // }
});