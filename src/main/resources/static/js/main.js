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

    let currentPage = 0;
    const size = 10; // Количество постов на одной странице
    let isLoading = false;

    // Функция для загрузки и добавления постов в контейнер
    function loadMorePosts() {
        if (isLoading) return;
        isLoading = true;
        $('#loading').show();

        $.ajax({
            url: `/loadMorePosts?page=${currentPage}&size=${size}`,
            type: 'GET',
            success: function(postsPage) {
                if (postsPage.content.length === 0) {
                    $('#loading').hide();
                    return;
                }
                postsPage.content.forEach(post => {
                    const postElement = createPostElement(post);
                    $('#feed').append(postElement);
                    // Инициализируем карусель Bootstrap для каждого добавленного элемента поста
                    if (post.images && post.images.length > 0) {
                        var carouselElement = document.querySelector('#carousel-' + post.id);
                        var carousel = new bootstrap.Carousel(carouselElement);
                    }
                });
                currentPage++;
                isLoading = false;
                $('#loading').hide();
            },
            error: function(error) {
                console.error('Error loading more posts:', error);
                isLoading = false;
                $('#loading').hide();
            }
        });
    }

    function createPostElement(post) {
        // Создаем внешний контейнер для поста
        const postElement = document.createElement('div');
        postElement.className = 'post rounded-3 p-3 my-2';
        postElement.setAttribute('data-post-id', post.id);

        // Создаем контейнер для информации о пользователе и посте
        const userContainer = document.createElement('div');
        userContainer.className = 'user border-bottom mb-2 pb-3';

        // Создаем изображение профиля
        const profilePicDiv = document.createElement('div');
        profilePicDiv.className = 'profile-pic';
        const profilePicImg = document.createElement('img');
        profilePicImg.className = 'profile-pic-img';
        profilePicImg.src = post.author.profilePicture ? `data:image/png;base64,${post.author.profilePicture}` : 'images/profile_image.png';
        profilePicImg.alt = 'Profile Picture';
        profilePicDiv.appendChild(profilePicImg);

        // Создаем информацию о посте (автор, дата)
        const postInfoDiv = document.createElement('div');
        postInfoDiv.className = 'post-info d-flex flex-column';
        const authorNameSpan = document.createElement('span');
        authorNameSpan.className = 'h4 mb-0';
        authorNameSpan.textContent = post.author.username; // Имя автора
        const dateDiv = document.createElement('div');
        dateDiv.className = 'text-white-50';
        const timeElement = document.createElement('time');

        // Форматируем дату и время в соответствии с вашим форматом
        const createdAt = new Date(post.createdAt);
        const options = { day: 'numeric', month: 'long', year: 'numeric', hour: '2-digit', minute: '2-digit', hour12: false };
        // Убедитесь, что используете 'ru-RU' для русского формата даты
        const formattedDate = createdAt.toLocaleDateString('ru-RU', options);

        timeElement.textContent = `${formattedDate}`;
        dateDiv.appendChild(timeElement);

        if (post.onlyForFollowers) {
            const followersSpan = document.createElement('span');
            followersSpan.className = 'ms-2';
            followersSpan.textContent = 'Для подписчиков';
            dateDiv.appendChild(followersSpan);
        }
        postInfoDiv.appendChild(authorNameSpan);
        postInfoDiv.appendChild(dateDiv);

        // Собираем контейнер пользователя
        userContainer.appendChild(profilePicDiv);
        userContainer.appendChild(postInfoDiv);

        // Создаем кнопку удаления поста, если текущий пользователь является автором
        if (post.author.username === $("#username").text()) {
            const gap = document.createElement('div');
            gap.className = 'gap flex-grow-1';

            const deletePostDiv = document.createElement('div');
            deletePostDiv.className = 'delete-post align-self-start';

            const deleteButton = document.createElement('button');
            deleteButton.setAttribute('type', 'button');
            deleteButton.className = 'delete-post-btn btn btn-danger p-0 d-flex';
            deleteButton.setAttribute('data-post-id', post.id);

            const deleteIcon = document.createElement('span');
            deleteIcon.className = 'material-icons';
            deleteIcon.textContent = 'delete';

            deleteButton.appendChild(deleteIcon);
            deletePostDiv.appendChild(deleteButton);

            // // Добавляем обработчик событий для кнопки удаления
            deleteButton.addEventListener('click', function() {
                // Здесь логика для удаления поста
                // Например, отправка AJAX запроса на сервер для удаления поста
                var csrfToken = $("meta[name='_csrf']").attr("content");
                var csrfHeader = $("meta[name='_csrf_header']").attr("content");
                console.log("Начинаю удаление")

                $.ajax({
                    url: '/delete-post/' + post.id, // Предполагается, что у вас есть такой маршрут на сервере
                    type: 'DELETE',
                    beforeSend: function(xhr) {
                        xhr.setRequestHeader(csrfHeader, csrfToken);
                    },
                    success: function(result) {
                        // Удаление поста из DOM или показ сообщения об успехе
                        console.log('Пост успешно удален');
                        // Например, удалить элемент поста из DOM
                        $('div.post[data-post-id="' + post.id + '"]').remove();
                    },
                    error: function(xhr, status, error) {
                        // Обработка ошибки
                        console.error('Ошибка удаления поста:', error);
                    }
                });
            });

            // Добавляем кнопку удаления в контейнер пользователя
            userContainer.appendChild(gap);
            userContainer.appendChild(deletePostDiv);
        }

        // Создаем текст поста
        const textDiv = document.createElement('div');
        textDiv.className = 'text';
        const postContentSpan = document.createElement('span');
        postContentSpan.className = 'post-content';
        postContentSpan.textContent = post.content; // Текст поста
        postContentSpan.style.whiteSpace = 'pre-wrap'; // Сохраняем форматирование текста
        textDiv.appendChild(postContentSpan);

        // Создаем кнопку "Показать еще"
        const showMoreBtn = document.createElement('button');
        showMoreBtn.className = 'show-more';
        showMoreBtn.textContent = 'Показать еще';
        // Добавляем обработчик событий для кнопки "Показать еще"
        showMoreBtn.addEventListener('click', function() {
            if (postContentSpan.classList.contains('collapsed')) {
                postContentSpan.classList.remove('collapsed'); // Раскрываем текст
                postContentSpan.classList.add('uncollapsed'); // Раскрываем текст
                showMoreBtn.textContent = 'Скрыть';
            } else {
                postContentSpan.classList.add('collapsed'); // Скрываем текст
                postContentSpan.classList.remove('uncollapsed'); // Раскрываем текст
                showMoreBtn.textContent = 'Показать еще';
            }
        });

        // Проверяем, нужно ли добавить класс collapsed и кнопку "Показать еще"
        // Это делается после добавления postContentSpan в DOM, чтобы корректно измерить высоту
        setTimeout(() => {
            if (postContentSpan.scrollHeight > postContentSpan.clientHeight+2) {
                postContentSpan.classList.add('collapsed');
                textDiv.appendChild(showMoreBtn);
            }
        }, 0);

        // Создаем контейнер для изображений
        const imagesContainer = document.createElement('div');
        imagesContainer.className = 'pictures mt-2';

        if (post.images && post.images.length > 0) {
            const carouselDiv = document.createElement('div');
            carouselDiv.className = 'carousel slide';
            carouselDiv.setAttribute('data-bs-ride', 'false');
            carouselDiv.setAttribute('data-bs-interval', 'false');
            carouselDiv.id = 'carousel-' + post.id;

            const carouselInnerDiv = document.createElement('div');
            carouselInnerDiv.className = 'carousel-inner';

            post.images.forEach((image, index) => {
                const carouselItemDiv = document.createElement('div');
                carouselItemDiv.className = 'carousel-item' + (index === 0 ? ' active' : '');

                const pictBgDiv = document.createElement('div');
                pictBgDiv.className = 'pict-bg rounded-4';
                pictBgDiv.style.background = `url(data:image/png;base64,${image}) no-repeat center`;
                pictBgDiv.style.backgroundSize = 'cover';

                const imgTag = document.createElement('img');
                imgTag.src = `data:image/png;base64,${image}`;
                imgTag.className = 'd-block w-100 rounded-4';
                imgTag.alt = 'Post image';

                pictBgDiv.appendChild(imgTag);
                carouselItemDiv.appendChild(pictBgDiv);
                carouselInnerDiv.appendChild(carouselItemDiv);
            });

            carouselDiv.appendChild(carouselInnerDiv);
            imagesContainer.appendChild(carouselDiv);

            // Добавляем элементы управления каруселью, если изображений больше одного
            if (post.images.length > 1) {
                const prevButton = document.createElement('button');
                prevButton.className = 'carousel-control-prev';
                prevButton.type = 'button';
                prevButton.setAttribute('data-bs-target', '#carousel-' + post.id);
                prevButton.setAttribute('data-bs-slide', 'prev');

                const prevIcon = document.createElement('span');
                prevIcon.className = 'carousel-control-prev-icon';
                prevIcon.setAttribute('aria-hidden', 'true');

                const prevText = document.createElement('span');
                prevText.className = 'visually-hidden';
                prevText.textContent = 'Previous';

                prevButton.appendChild(prevIcon);
                prevButton.appendChild(prevText);

                const nextButton = document.createElement('button');
                nextButton.className = 'carousel-control-next';
                nextButton.type = 'button';
                nextButton.setAttribute('data-bs-target', '#carousel-' + post.id);
                nextButton.setAttribute('data-bs-slide', 'next');

                const nextIcon = document.createElement('span');
                nextIcon.className = 'carousel-control-next-icon';
                nextIcon.setAttribute('aria-hidden', 'true');

                const nextText = document.createElement('span');
                nextText.className = 'visually-hidden';
                nextText.textContent = 'Next';

                nextButton.appendChild(nextIcon);
                nextButton.appendChild(nextText);

                carouselDiv.appendChild(prevButton);
                carouselDiv.appendChild(nextButton);

                // Добавляем индикаторы для карусели
                const carouselIndicators = document.createElement('div');
                carouselIndicators.className = 'carousel-indicators';

                post.images.forEach((_, index) => {
                    const indicatorButton = document.createElement('button');
                    indicatorButton.type = 'button';
                    indicatorButton.setAttribute('data-bs-target', '#carousel-' + post.id);
                    indicatorButton.setAttribute('data-bs-slide-to', index);
                    indicatorButton.className = index === 0 ? 'active' : '';
                    indicatorButton.setAttribute('aria-current', 'true');
                    indicatorButton.setAttribute('aria-label', `Slide ${index + 1}`);

                    carouselIndicators.appendChild(indicatorButton);
                });

                carouselDiv.appendChild(carouselIndicators);
            }
        }

        // Собираем и возвращаем полный пост
        postElement.appendChild(userContainer);
        postElement.appendChild(textDiv);
        postElement.appendChild(imagesContainer);

        return postElement;
    }

    $('#feed').on('scroll', function() {
        if ($(this).scrollTop() + $(this).innerHeight() >= this.scrollHeight) {
            loadMorePosts();
        }
    });

    // Загрузите первую страницу постов при инициализации
    loadMorePosts();

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

    // $('.carousel').carousel();
});