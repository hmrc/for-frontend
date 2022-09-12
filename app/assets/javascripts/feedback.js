(function ($) {

    VoaFeedback.toggleHelp = function(){
        $('.form-help-toggle').click(function(e){
            e.preventDefault();
            $('#helpForm').toggle();
        });
    };

    VoaFeedback.helpForm = function() {
        $(document).on('submit','#helpForm form', function(event) {
            event.preventDefault();
            $('#report-submit').prop('disabled', true);
            var data = VoaFeedback.prepareData($(this));
            $('.contact-form-copy').addClass('hidden');
            $('.form--feedback').addClass('hidden');
            $('#helpFormWrapper').addClass('hidden');
            $('.spinner').show();

            $.ajax({
                type: 'POST',
                url:  '/' + VoaFor.service() + '/pageHelp',
                data: $(this).serialize() + '&referer=' + window.location.href
            });

            $.ajax({
                type: 'POST',
                url:  $(this).attr('action'),
                data: data,
                success: function() {
                    window.setTimeout(function () {
                        $('.feedback-thankyou').removeClass('hidden');
                        $('.spinner').hide();
                    }, 1500);
                },
                error: function(error) {
                    window.setTimeout(function () {
                        $('.spinner').hide();
                        $('#helpFormWrapper').html('<p>'+error.responseText+'</p>');
                        $('.contact-form-copy').addClass('hidden');
                        $('.feedback-thankyou').removeClass('hidden');
                        $('.form--feedback').addClass('hidden');
                    }, 1500);
                }
            });
        });
    };

    VoaFeedback.prepareData = function (_data) {
        var data = _data.serializeArray().reduce(function(obj, item) {
            obj[item.name] = item.value;
            return obj;
        }, {});

        var referenceNumber = $('div.for-id').attr('for-id');
        if(referenceNumber === undefined) {
            referenceNumber = 'not provided';
        }

        var replaceRegex = new RegExp('https?://');

        data['report-error'] = data['report-error'] + '\n\nRef : ' + referenceNumber + '\n' +
            (String(window.location.href).replace(replaceRegex, ''));
        data['referer'] = String(window.location.href);
        data['isJavascript'] = true;

        return data;
    };


})(jQuery);
