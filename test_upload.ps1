
$fileContent = "This is a test document for knowledge base."
$filePath = "test_upload.txt"
$fileContent | Out-File -FilePath $filePath -Encoding utf8

$uri = "http://localhost:8080/api/docs/upload"
$Form = @{
    file = Get-Item -Path $filePath
}

try {
    $result = Invoke-RestMethod -Uri $uri -Method Post -Form $Form
    Write-Host "Upload Success:"
    $result | ConvertTo-Json
} catch {
    Write-Host "Upload Failed:"
    $_.Exception.Message
    if ($_.ErrorDetails) {
        $_.ErrorDetails.Message
    }
    # Check if we can get the response body
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Response Body:"
        $responseBody
    }
} finally {
    Remove-Item $filePath
}
