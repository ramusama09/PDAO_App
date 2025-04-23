document.addEventListener('DOMContentLoaded', function() {
    // DOM elements
    const signatureCanvas = document.getElementById('signatureCanvas');
    const clearSignatureBtn = document.getElementById('clearSignature');
    const startCameraBtn = document.getElementById('startCamera');
    const captureImageBtn = document.getElementById('captureImage');
    const cameraFeed = document.getElementById('cameraFeed');
    const captureCanvas = document.getElementById('captureCanvas');
    const capturedImage = document.getElementById('capturedImage');
    const previewButton = document.getElementById('previewButton');
    const printButton = document.getElementById('printButton');
    const frontIdPreview = document.getElementById('frontIdPreview');
    const backIdPreview = document.getElementById('backIdPreview');
    const printArea = document.getElementById('printArea');
    
    let stream = null;
    let isDrawing = false;
    let signatureDataURL = null;
    let photoDataURL = null;
    
    // Initialize signature canvas
    const signatureCtx = signatureCanvas.getContext('2d');
    signatureCtx.lineWidth = 2;
    signatureCtx.lineCap = 'round';
    signatureCtx.strokeStyle = 'black';

    // Set initial background color for canvas
    signatureCtx.fillStyle = '#fff';
    signatureCtx.fillRect(0, 0, signatureCanvas.width, signatureCanvas.height);
    // Initialize default signature
    signatureDataURL = signatureCanvas.toDataURL('image/png');

    // Set default photo
    capturedImage.src = './img/default_face.jpg';
    photoDataURL = './img/default_face.jpg';

    // Signature canvas event listeners
    signatureCanvas.addEventListener('mousedown', startDrawing);
    signatureCanvas.addEventListener('mousemove', draw);
    signatureCanvas.addEventListener('mouseup', stopDrawing);
    signatureCanvas.addEventListener('mouseout', stopDrawing);
    signatureCanvas.addEventListener('touchstart', startDrawingTouch);
    signatureCanvas.addEventListener('touchmove', drawTouch);
    signatureCanvas.addEventListener('touchend', stopDrawing);
    
    clearSignatureBtn.addEventListener('click', clearSignature);
    
    // Camera functionality
    startCameraBtn.addEventListener('click', startCamera);
    captureImageBtn.addEventListener('click', captureImage);
    
    // Preview and print buttons
    previewButton.addEventListener('click', previewID);
    printButton.addEventListener('click', printIDs);
    
    // Signature functions
    function startDrawing(e) {
        isDrawing = true;
        signatureCtx.beginPath();
        signatureCtx.moveTo(e.offsetX, e.offsetY);
    }
    
    function draw(e) {
        if (!isDrawing) return;
        signatureCtx.lineTo(e.offsetX, e.offsetY);
        signatureCtx.stroke();
    }
    
    function startDrawingTouch(e) {
        e.preventDefault();
        const touch = e.touches[0];
        const rect = signatureCanvas.getBoundingClientRect();
        const offsetX = touch.clientX - rect.left;
        const offsetY = touch.clientY - rect.top;
        
        isDrawing = true;
        signatureCtx.beginPath();
        signatureCtx.moveTo(offsetX, offsetY);
    }
    
    function drawTouch(e) {
        e.preventDefault();
        if (!isDrawing) return;
        
        const touch = e.touches[0];
        const rect = signatureCanvas.getBoundingClientRect();
        const offsetX = touch.clientX - rect.left;
        const offsetY = touch.clientY - rect.top;
        
        signatureCtx.lineTo(offsetX, offsetY);
        signatureCtx.stroke();
    }
    
    function stopDrawing() {
        if (isDrawing) {
            signatureCtx.closePath();
            isDrawing = false;
            signatureDataURL = signatureCanvas.toDataURL('image/png');
        }
    }
    
    function clearSignature() {
        signatureCtx.clearRect(0, 0, signatureCanvas.width, signatureCanvas.height);
        signatureCtx.fillStyle = '#fff';
        signatureCtx.fillRect(0, 0, signatureCanvas.width, signatureCanvas.height);
        signatureDataURL = signatureCanvas.toDataURL('image/png');
    }
    
    // Camera functions
    async function startCamera() {
        try {
            stream = await navigator.mediaDevices.getUserMedia({ 
                video: {
                    width: { ideal: 300 },
                    height: { ideal: 225 }
                }
            });
            cameraFeed.srcObject = stream;
            startCameraBtn.disabled = true;
            captureImageBtn.disabled = false;
        } catch (err) {
            console.error('Error accessing camera:', err);
            alert('Error accessing camera. Please make sure you have granted permission to use the camera.');
        }
    }
    
    function captureImage() {
        if (!stream) return;
        
        const captureCtx = captureCanvas.getContext('2d');
        captureCtx.drawImage(cameraFeed, 0, 0, captureCanvas.width, captureCanvas.height);
        photoDataURL = captureCanvas.toDataURL('image/png');
        capturedImage.src = photoDataURL;
        
        // Stop the camera stream
        stream.getTracks().forEach(track => track.stop());
        stream = null;
        cameraFeed.srcObject = null;
        startCameraBtn.disabled = false;
    }
    
    // Preview ID function
    function previewID() {
        const formData = getFormData();
        
        // Create front ID preview using our template
        frontIdPreview.innerHTML = `
            <div class="card pwd-card">
                <div class="header">
                    <div class="logo-container">
                        <img src="./img/PH_Flag.webp" alt="Philippine Flag" class="flag">
                        <div class="text-center">
                            <h5 class="mb-0 fw-bold">REPUBLIC OF THE PHILIPPINES</h5>
                            <h4 class="mb-0 helvetica shadowed">CITY OF PARAÑAQUE</h4>
                            <small>Persons with Disability Affairs Office</small>
                        </div>
                        <div class="logo-group">
                            <img src="./img/PQ_Logo.jpg" alt="City Seal" class="city-seal">
                            <img src="./img/BP_Logo.jpg" alt="Bagong Pilipinas Logo" class="other-logo">
                            <img src="./img/PDAO_Logo.png" alt="PWD Logo" class="other-logo">
                        </div>
                    </div>
                    <div class="mt-3">
                        <div class="row">
                            <div class="col-md-3"></div>
                            <div class="col-md-9">
                                <strong>PWD ID NO: </strong><span class="underlined-text text-space">${formData.pwdIdNo || '0000-0000-0000-0000'}</span>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="content">
                    <div class="row">
                        <div class="col-md-3">
                            <div class="photo-box p-1">
                                <img src="${formData.photo || './img/default_face.jpg'}" alt="ID Photo">
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="field">
                                <div class="field-value">${formData.name || 'Juan Dela Cruz'}</div>
                                <div class="field-line"></div>
                                <div class="field-label">NAME</div>
                            </div>
                            <div class="field">
                                <div class="field-value">${formData.disabilityType || 'Insert Disability Here'}</div>
                                <div class="field-line"></div>
                                <div class="field-label">TYPE OF DISABILITY</div>
                            </div>
                            <div class="field">
                                <div class="field-value">
                                    <img src="${formData.signature || signatureCanvas.toDataURL()}" alt="Signature" style="max-height: 50px; max-width: 100%;">
                                </div>
                                <div class="field-line"></div>
                                <div class="field-label">SIGNATURE</div>
                            </div>
                        </div>
                        <div class="col-md-3">
                            <div class="photo-box-no-border p-1">
                                <img src="./img/qr_sample.png" alt="QR Code">
                            </div>
                        </div>
                    </div>
                    <div class="row mt-3">
                        <div class="col">
                            <strong>Expires on: </strong><span class="underlined-text">${formData.expiresOn || 'Month DD, 20XX'}</span>
                        </div>
                    </div>
                </div>
                <div class="footer">
                    VALID ANYWHERE IN THE PHILIPPINES
                </div>
            </div>
        `;
        
        // Create back ID preview using our template
        backIdPreview.innerHTML = `
            <div class="card emergency-card">
                <h2>THIS IS NON-TRANSFERABLE</h2>
                <img src="./img/PQ_Logo.jpg" alt="City Seal Watermark" class="watermark">
                
                <div class="form-row">
                    <div class="flex-row">
                        <label>ADDRESS:</label>
                        <div class="input-line">${formData.address || '123 Main Street, Example Building'}</div>
                    </div>
                </div>
                
                <div class="form-row half-row">
                    <div>
                        <div class="flex-row">
                            <label>DATE OF BIRTH:</label>
                            <div class="input-line">${formData.dateOfBirth || 'January 15, 1995'}</div>
                        </div>
                    </div>
                    <div>
                        <div class="flex-row">
                            <label>SEX:</label>
                            <div class="input-line">${formData.sex || 'Male'}</div>
                        </div>
                    </div>
                </div>
                
                <div class="form-row half-row">
                    <div>
                        <div class="flex-row">
                            <label>DATE ISSUED:</label>
                            <div class="input-line">${formData.dateIssued || 'April 23, 2025'}</div>
                        </div>
                    </div>
                    <div>
                        <div class="flex-row">
                            <label>BLOOD TYPE:</label>
                            <div class="input-line">${formData.bloodType || 'O+'}</div>
                        </div>
                    </div>
                </div>
                
                <div class="emergency-title">
                    IN CASE OF EMERGENCY PLEASE NOTIFY:
                </div>
                
                <div class="form-row">
                    <div class="flex-row">
                        <label>PARENT/GUARDIAN:</label>
                        <div class="input-line">${formData.guardian || 'John Doe Periodt'}</div>
                    </div>
                </div>
                
                <div class="form-row">
                    <div class="flex-row">
                        <label>CONTACT NO.:</label>
                        <div class="input-line">${formData.contactNumber || '0912-345-6789'}</div>
                    </div>
                </div>
                
                <div class="footer-content">
                    <h3 class="helvetica shadowed">HON. ERIC L. OLIVAREZ</h3>
                    <p class="fw-bold">City Mayor</p>
                </div>
            </div>
        `;
    }
    
    // Print IDs function
    function printIDs() {
        const formData = getFormData();
        
        // Clear previous print area
        printArea.innerHTML = '';
        
        // Create print layout with single front and back card
        const printContainer = document.createElement('div');
        printContainer.className = 'print-layout';
        
        // Create front card for printing
        const frontCardHtml = `
            <div class="pwd-card">
                <div class="header">
                    <div class="logo-container">
                        <img src="./img/PH_Flag.webp" alt="Philippine Flag" class="flag">
                        <div class="text-center">
                            <h5 class="mb-0 fw-bold">REPUBLIC OF THE PHILIPPINES</h5>
                            <h4 class="mb-0 helvetica shadowed">CITY OF PARAÑAQUE</h4>
                            <small>Persons with Disability Affairs Office</small>
                        </div>
                        <div class="logo-group">
                            <img src="./img/PQ_Logo.jpg" alt="City Seal" class="city-seal">
                            <img src="./img/BP_Logo.jpg" alt="Bagong Pilipinas Logo" class="other-logo">
                            <img src="./img/PDAO_Logo.png" alt="PWD Logo" class="other-logo">
                        </div>
                    </div>
                    <div class="mt-3">
                        <div class="row">
                            <div class="col-md-3"></div>
                            <div class="col-md-9">
                                <strong>PWD ID NO: </strong><span class="underlined-text text-space">${formData.pwdIdNo || '0000-0000-0000-0000'}</span>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="content">
                    <div class="row">
                        <div class="col-md-3">
                            <div class="photo-box p-1">
                                <img src="${formData.photo || './img/default_face.jpg'}" alt="ID Photo">
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="field">
                                <div class="field-value">${formData.name || 'Juan Dela Cruz'}</div>
                                <div class="field-line"></div>
                                <div class="field-label">NAME</div>
                            </div>
                            <div class="field">
                                <div class="field-value">${formData.disabilityType || 'Insert Disability Here'}</div>
                                <div class="field-line"></div>
                                <div class="field-label">TYPE OF DISABILITY</div>
                            </div>
                            <div class="field">
                                <div class="field-value">
                                    <img src="${formData.signature || signatureCanvas.toDataURL()}" alt="Signature" style="max-height: 50px; max-width: 100%;">
                                </div>
                                <div class="field-line"></div>
                                <div class="field-label">SIGNATURE</div>
                            </div>
                        </div>
                        <div class="col-md-3">
                            <div class="photo-box-no-border p-1">
                                <img src="./img/qr_sample.png" alt="QR Code">
                            </div>
                        </div>
                    </div>
                    <div class="row mt-3">
                        <div class="col">
                            <strong>Expires on: </strong><span class="underlined-text">${formData.expiresOn || 'Month DD, 20XX'}</span>
                        </div>
                    </div>
                </div>
                <div class="footer">
                    VALID ANYWHERE IN THE PHILIPPINES
                </div>
            </div>
        `;
        
        // Create back card for printing
        const backCardHtml = `
            <div class="emergency-card">
                <h2>THIS IS NON-TRANSFERABLE</h2>
                <img src="./img/PQ_Logo.jpg" alt="City Seal Watermark" class="watermark">
                
                <div class="form-row">
                    <div class="flex-row">
                        <label>ADDRESS:</label>
                        <div class="input-line">${formData.address || '123 Main Street, Example Building'}</div>
                    </div>
                </div>
                
                <div class="form-row half-row">
                    <div>
                        <div class="flex-row">
                            <label>DATE OF BIRTH:</label>
                            <div class="input-line">${formData.dateOfBirth || 'January 15, 1995'}</div>
                        </div>
                    </div>
                    <div>
                        <div class="flex-row">
                            <label>SEX:</label>
                            <div class="input-line">${formData.sex || 'Male'}</div>
                        </div>
                    </div>
                </div>
                
                <div class="form-row half-row">
                    <div>
                        <div class="flex-row">
                            <label>DATE ISSUED:</label>
                            <div class="input-line">${formData.dateIssued || 'April 23, 2025'}</div>
                        </div>
                    </div>
                    <div>
                        <div class="flex-row">
                            <label>BLOOD TYPE:</label>
                            <div class="input-line">${formData.bloodType || 'O+'}</div>
                        </div>
                    </div>
                </div>
                
                <div class="emergency-title">
                    IN CASE OF EMERGENCY PLEASE NOTIFY:
                </div>
                
                <div class="form-row">
                    <div class="flex-row">
                        <label>PARENT/GUARDIAN:</label>
                        <div class="input-line">${formData.guardian || 'John Doe Periodt'}</div>
                    </div>
                </div>
                
                <div class="form-row">
                    <div class="flex-row">
                        <label>CONTACT NO.:</label>
                        <div class="input-line">${formData.contactNumber || '0912-345-6789'}</div>
                    </div>
                </div>
                
                <div class="footer-content">
                    <h3 class="helvetica shadowed">HON. ERIC L. OLIVAREZ</h3>
                    <p class="fw-bold">City Mayor</p>
                </div>
            </div>
        `;
        
        // Create front and back card containers
        const frontCardContainer = document.createElement('div');
        frontCardContainer.innerHTML = frontCardHtml;
        
        const backCardContainer = document.createElement('div');
        backCardContainer.innerHTML = backCardHtml;
        
        // Add to print layout
        printContainer.appendChild(frontCardContainer);
        printContainer.appendChild(backCardContainer);
        printArea.appendChild(printContainer);
        
        // Trigger print
        window.print();
    }
    
    // Helper functions
    function getFormData() {
        return {
            pwdIdNo: document.getElementById('pwdIdNo').value,
            name: document.getElementById('name').value,
            disabilityType: document.getElementById('disabilityType').value,
            expiresOn: formatDate(document.getElementById('expiresOn').value),
            signature: signatureDataURL,
            photo: photoDataURL,
            address: document.getElementById('address').value,
            dateOfBirth: formatDate(document.getElementById('dateOfBirth').value),
            sex: document.getElementById('sex').value,
            dateIssued: formatDate(document.getElementById('dateIssued').value),
            bloodType: document.getElementById('bloodType').value,
            guardian: document.getElementById('guardian').value,
            contactNumber: document.getElementById('contactNumber').value
        };
    }
    
    function formatDate(dateString) {
        if (!dateString) return '';
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', { 
            month: 'long',
            day: 'numeric',
            year: 'numeric'
        });
    }
}); 