function makeAPICall() {
    let departureCity = document.getElementById("departureCity").value;
    let arrivalCity = document.getElementById("arrivalCity").value;
    jQuery(document).ready(function () {
        jQuery.ajax({
            url: 'http://localhost:8080/api/v1/getNearestFlight?departureCity=' + departureCity + '&arrivalCity=' + arrivalCity,
            type: 'GET',
            success: function (result) {
                let table = document.querySelector('table');
                let departureTime = new Date(result['departureDateTimeLocal']).toLocaleTimeString([], {
                    hour: "2-digit",
                    minute: "2-digit"
                })
                let arrivalTime = new Date(result['arrivalDateTimeLocal']).toLocaleTimeString([], {
                    hour: "2-digit",
                    minute: "2-digit"
                })
                table.innerHTML = '<tr><td><h4>' + departureCity + '</h4><p>' + result['departureAirport'] + '</p><h1>' + departureTime + '</h1></td><td>' +
                    arrowHTMLText + '<p>' + result['timeInFlight'] + '</p><p>' + result['airline'] + '</p></td>' +
                '<td><h4>' + arrivalCity + '</h4><p>' + result['arrivalAirport'] + '</p><h1>' + arrivalTime + '</h1></td></tr>';
            },
            error: function (xhr, status, error) {
                console.log(error);
                let errorInfo = JSON.parse(xhr.responseText)
                let table = document.querySelector('table')
                table.innerHTML = '<tr><td>' + errorInfo.message + '</td></tr>';
            }
        })
    })
}

let arrowHTMLText = '<svg viewBox="0 0 134.13 33.47" xmlns="http://www.w3.org/2000/svg" width="135" height="34" fill="#0098db" stroke="#0098db">' +
    '<circle cx="3" cy="30.47" r="3" stroke="none"></circle>' +
    '<circle cx="131.13" cy="30.47" r="3" stroke="none"></circle>' +
    '<g fill="none" stroke-linecap="round" stroke-linejoin="round" stroke-width="2">' +
    '<path d="m94.96 1 4.29 7.95-7.94 4.3"></path>' +
    '<path d="m98.23 8.67a102 102 0 0 0 -31.16-4.73 99 99 0 0 0 -38.3 7.38c-11.05 4.68-20.02 11.3-25.77 19.15"></path>' +
    '<path d="m131.13 30.47a56.73 56.73 0 0 0 -18.86-15.83"></path></g></svg>'