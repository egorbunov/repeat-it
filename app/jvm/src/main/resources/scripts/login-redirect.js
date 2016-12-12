
//var $loginForm = $('#login_form');
//var $logoutForm = $('#logout_form');
//var $currentLogin = $('#current_login');
//var $currentLoginStatus = $('#current_login_status_code');
//$loginForm.hide();
//$logoutForm.hide();

//function handleResponse(data, status) {
//    if (status == 'success') {

//        $loginForm.hide();
//        $logoutForm.show();
//        $currentLogin.text(data);
//        $currentLoginStatus.text(status);
//    } else {
//        window.location.href = '/site/login.html'
//        $loginForm.show();
//        $logoutForm.hide();
//        $currentLogin.text('');
//        $currentLoginStatus.text(status + ' (' + data.status + ')');
    }
}

//function getCurrentLogin() {
//    $.ajax(addCsrfHeader({
//        url: '/api/current_login',
//        type: 'GET',
//        success: handleResponse,
//        error: handleResponse
//    }));
//}
//
//getCurrentLogin();

//$('#do_login').click(function(e) {
//    $.ajax(addCsrfHeader({
//        url: '/api/do_login',
//        type: 'POST',
//        data: $('#login').val(),
//        success: getCurrentLogin
//    }));
//    e.preventDefault();
//    return false;
//});
//
//$('#do_logout').click(function(e) {
//    $.ajax(addCsrfHeader({
//        url: '/api/do_logout',
//        type: 'POST',
//        success: getCurrentLogin
//    }));
//    e.preventDefault();
//    return false;
//});

//function addCsrfHeader(opts) {
//    var token = Cookies.get('XSRF-TOKEN');
//    if (token) {
//        console.log('Setting csrf token: ' + token);
//        opts['headers'] = {
//            'X-XSRF-TOKEN': token
//        }
//    } else {
//        console.log('No csrf token');
//    }
//    return opts;
//}