source "azure-arm" "image-template" {
  subscription_id                   = "009ed339-7c35-4c95-98e5-7b1cbe4357b1"
  tenant_id                         = "b1997eae-0fbe-4c39-9837-392933467165"
  client_id                         = "4827ba0b-73d4-49fa-b011-695cdbe2119b"
  client_secret                     = "009ed339-7c35-4c95-98e5-7b1cbe4357b1"
  managed_image_name                = "myfirstimage"
  managed_image_resource_group_name = "sre-dev-001"
  communicator                      = "winrm"
  image_offer                       = "WindowsServer"
  image_publisher                   = "MicrosoftWindowsServer"
  image_sku                         = "2019-Datacenter"
  location                          = "eastus"
  os_type                           = "Windows"
  vm_size                           = "Standard_B4ms"
  winrm_insecure                    = "true"
  winrm_timeout                     = "5m"
  winrm_use_ssl                     = "true"
  winrm_username                    = "packer"
}

build {
  sources = ["source.azure-arm.image-template"]

  provisioner "powershell" {
    inline = ["Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))",
    "choco install vlc --yes"]
  }

  provisioner "powershell" {
    inline       = ["while ((Get-Service RdAgent).Status -ne 'Running') { Start-Sleep -s 5 }", "while ((Get-Service WindowsAzureGuestAgent).Status -ne 'Running') { Start-Sleep -s 5 }", "& $env:SystemRoot\\System32\\Sysprep\\Sysprep.exe /oobe /generalize /quiet /quit", "while($true) { $imageState = Get-ItemProperty HKLM:\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Setup\\State | Select ImageState; if($imageState.ImageState -ne 'IMAGE_STATE_GENERALIZE_RESEAL_TO_OOBE') { Write-Output $imageState.ImageState; Start-Sleep -s 10  } else { break } }"]
    pause_before = "2m0s"
  }

}
